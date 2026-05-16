package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.constant.CacheConstants;
import net.zicai.dto.BenefitDTO;
import net.zicai.dto.ProductPackageDTO;
import net.zicai.mapper.BenefitMapper;
import net.zicai.mapper.PackageBenefitMapper;
import net.zicai.mapper.ProductPackageMapper;
import net.zicai.model.BenefitDO;
import net.zicai.model.PackageBenefitDO;
import net.zicai.model.ProductPackageDO;
import net.zicai.service.ProductPackageService;
import net.zicai.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 套餐服务实现类
 *
 * @author wangdi
 * @date 2026/5/7 15:55
 * @description 包含Redis缓存实现：
 * 1. 缓存穿透防护：空值缓存
 * 2. 缓存击穿防护：互斥锁 + 永不过期策略
 * 3. 缓存过期策略：30分钟TTL
 */
@Service
@Slf4j
public class ProductPackageServiceImpl implements ProductPackageService {

    @Autowired
    private BenefitMapper benefitMapper;

    @Autowired
    private ProductPackageMapper productPackageMapper;

    @Autowired
    private PackageBenefitMapper packageBenefitMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Redis互斥锁Lua脚本（SET NX EX原子操作）
     */
    private static final String LOCK_SCRIPT =
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
            "redis.call('expire', KEYS[1], ARGV[2]) return 1 else return 0 end";

    private DefaultRedisScript<Long> lockScript;

    @PostConstruct
    public void init() {
        lockScript = new DefaultRedisScript<>();
        lockScript.setScriptText(LOCK_SCRIPT);
        lockScript.setResultType(Long.class);
    }

    @Override
    public List<ProductPackageDTO> listPackages(String status) {
        String cacheKey = buildCacheKey(status);

        // 1. 先从缓存获取
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            // 缓存命中
            if ("NULL_MARKER".equals(cachedData)) {
                // 空值缓存（缓存穿透防护）- 查到数据库也没有数据
                log.debug("缓存穿透防护：返回空列表, cacheKey:{}", cacheKey);
                return Collections.emptyList();
            }
            log.debug("缓存命中, cacheKey:{}", cacheKey);
            return parseCachedData(cachedData);
        }

        // 2. 缓存未命中，尝试获取锁防止缓存击穿
        String lockKey = CacheConstants.PRODUCT_PACKAGE_LIST_LOCK_KEY + ":" + status;
        boolean lockAcquired = tryAcquireLock(lockKey);

        try {
            if (lockAcquired) {
                // 获取锁成功，双重检查（可能其他线程已加载）
                cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cachedData != null) {
                    if ("NULL_MARKER".equals(cachedData)) {
                        return Collections.emptyList();
                    }
                    return parseCachedData(cachedData);
                }

                // 从数据库查询
                List<ProductPackageDTO> result = queryFromDatabase(status);

                // 写入缓存
                saveToCache(cacheKey, result);

                return result;
            } else {
                // 未获取到锁，短暂等待后重试缓存
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cachedData != null) {
                    if ("NULL_MARKER".equals(cachedData)) {
                        return Collections.emptyList();
                    }
                    return parseCachedData(cachedData);
                }

                // 仍然未命中，直接查询数据库（不缓存，避免击穿）
                log.warn("获取锁超时，直接查询数据库, status:{}", status);
                return queryFromDatabase(status);
            }
        } finally {
            // 释放锁（如果获取成功）
            if (lockAcquired) {
                releaseLock(lockKey);
            }
        }
    }

    @Override
    public List<BenefitDTO> getPackageBenefits(Long packageId) {
        LambdaQueryWrapper<PackageBenefitDO> queryWrapper = new LambdaQueryWrapper<PackageBenefitDO>();
        queryWrapper.eq(PackageBenefitDO::getProductPackageId, packageId);
        List<PackageBenefitDO> packageBenefitDOList = packageBenefitMapper.selectList(queryWrapper);
        //转换为DTO
        return packageBenefitDOList.stream().map(packageBenefitDO -> {
            BenefitDO benefitDO = benefitMapper.selectOne(new LambdaQueryWrapper<BenefitDO>()
                    .eq(BenefitDO::getBenefitCode, packageBenefitDO.getBenefitCode()));
            return SpringBeanUtil.copyProperties(benefitDO, BenefitDTO.class);
        }).collect(Collectors.toList());
    }

    /**
     * 构建缓存Key
     */
    private String buildCacheKey(String status) {
        return CacheConstants.PRODUCT_PACKAGE_LIST_KEY + ":" + (status != null ? status : "all");
    }

    /**
     * 从数据库查询套餐列表
     */
    private List<ProductPackageDTO> queryFromDatabase(String status) {
        log.info("从数据库查询套餐列表, status:{}", status);
        LambdaQueryWrapper<ProductPackageDO> queryWrapper = new LambdaQueryWrapper<ProductPackageDO>();
        if (status != null) {
            queryWrapper.eq(ProductPackageDO::getStatus, status);
        }
        queryWrapper.orderByAsc(ProductPackageDO::getSort);
        List<ProductPackageDO> productPackageDOList = productPackageMapper.selectList(queryWrapper);

        // 将套餐填充权益信息
        return productPackageDOList.stream().map(productPackageDO -> {
            List<BenefitDTO> packageBenefits = getPackageBenefits(productPackageDO.getId());
            ProductPackageDTO dto = SpringBeanUtil.copyProperties(productPackageDO, ProductPackageDTO.class);
            dto.setBenefits(packageBenefits);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 保存到缓存
     */
    private void saveToCache(String cacheKey, List<ProductPackageDTO> result) {
        if (result == null || result.isEmpty()) {
            // 空结果，使用空值缓存（缓存穿透防护）
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    "NULL_MARKER",
                    CacheConstants.NULL_VALUE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
            log.info("缓存空值, cacheKey:{}", cacheKey);
        } else {
            // 正常结果缓存，30分钟TTL
            String jsonData = toJson(result);
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    jsonData,
                    CacheConstants.PRODUCT_PACKAGE_LIST_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
            log.info("缓存套餐列表, cacheKey:{}, count:{}", cacheKey, result.size());
        }
    }

    /**
     * 解析缓存数据
     */
    private List<ProductPackageDTO> parseCachedData(String jsonData) {
        return SpringBeanUtil.json2List(jsonData, ProductPackageDTO.class);
    }

    /**
     * 对象转JSON
     */
    private String toJson(Object obj) {
        return SpringBeanUtil.obj2Json(obj);
    }

    /**
     * 尝试获取分布式锁
     */
    private boolean tryAcquireLock(String lockKey) {
        try {
            Long result = stringRedisTemplate.execute(
                    lockScript,
                    Collections.singletonList(lockKey),
                    "1",
                    "10"
            );
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("获取锁异常, lockKey:{}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     */
    private void releaseLock(String lockKey) {
        try {
            stringRedisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("释放锁异常, lockKey:{}", lockKey, e);
        }
    }

    /**
     * 清除套餐列表缓存（数据变更时调用）
     */
    public void evictPackageCache() {
        // 清除所有状态的缓存
        stringRedisTemplate.delete(CacheConstants.PRODUCT_PACKAGE_LIST_KEY + ":ON");
        stringRedisTemplate.delete(CacheConstants.PRODUCT_PACKAGE_LIST_KEY + ":OFF");
        stringRedisTemplate.delete(CacheConstants.PRODUCT_PACKAGE_LIST_KEY + ":all");
        log.info("清除套餐列表缓存完成");
    }
}