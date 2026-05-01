package net.zicai.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import net.zicai.service.OssService;
import net.zicai.config.OssClientManager;
import net.zicai.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author 王镝
 * @date 20260423
 **/
@Service
@Slf4j
public class OssServiceImpl implements OssService {

    /**
     * 预览URL过期时间，设置为1小时（毫秒）
     */
    private static final long PREVIEW_URL_EXPIRATION_MILLIS = 3600 * 1000L; // 1小时

    @Autowired
    private OssClientManager ossClientManager;

    /**
     * 上传安全文件到私有存储桶
     *
     * @param file   要上传的文件
     * @param folder 文件存放的目录
     * @return 上传后的文件访问URL
     */
    @Override
    public String uploadSecureFile(MultipartFile file, String folder) {
        return uploadToBucket(file, folder, ossClientManager.getSecureBucket());
    }

    /**
     * 上传公共文件到公共存储桶
     *
     * @param file   要上传的文件
     * @param folder 文件存放的目录
     * @return 上传后的文件访问URL
     */
    @Override
    public String uploadPublicFile(MultipartFile file, String folder) {
        return uploadToBucket(file, folder, ossClientManager.getPublicBucket());
    }

    /**
     * 将文件上传到指定的存储桶
     *
     * @param file   要上传的文件
     * @param folder 文件存放的目录
     * @param bucket 目标存储桶客户端
     * @return 上传后的文件访问URL
     */
    private String uploadToBucket(MultipartFile file, String folder, OssClientManager.BucketClient bucket) {
        validateBucket(bucket);
        try {
            String fileName = FileUtil.generateUniqueFileName(file.getOriginalFilename());
            String filePath = FileUtil.buildFilePath(folder, fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                bucket.getClient().putObject(bucket.getBucketName(), filePath, inputStream, metadata);
            }

            String fileUrl = bucket.buildFileKey(filePath);
            log.info("文件上传成功，文件名：{}，URL：{}", fileName, fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 验证存储桶配置是否有效
     *
     * @param bucket 待验证的存储桶客户端
     * @throws IllegalStateException 当存储桶配置无效时抛出异常
     */
    private void validateBucket(OssClientManager.BucketClient bucket) {
        if (bucket == null) {
            throw new IllegalStateException("目标存储空间未配置，无法上传文件");
        }
        if (!bucket.hasBucket()) {
            throw new IllegalStateException("OSS Bucket未配置，无法上传文件");
        }
        if (!bucket.hasClient()) {
            throw new IllegalStateException("OSS Client未配置，无法上传文件");
        }
    }

    /**
     * 判断Bucket属于哪个存储同
     */
     private OssClientManager.BucketClient getBucketClient(String fileUrl) {
         return ossClientManager.resolveByUrl(fileUrl);
     }

    /**
     * 删除指定Url得文件
     * 1 判断bucket类型
     * 2 获取fileKey
     * 3 删除操作
     * @param fileUrl 文件URL
     * @return
     */
    @Override
    public boolean deleteFile(String fileUrl) {

        try{
            OssClientManager.BucketClient bucket = getBucketClient(fileUrl);
            String fileKey = extractFileKey(fileUrl);

            if (StringUtils.isBlank(fileUrl)) {
                log.error("无法从Url中提取文件，请重新输入");
                return false;
            }
            bucket.getClient().deleteObject(bucket.getBucketName(), fileKey);
            log.info("文件删除成功：fileKey={}", fileKey);
            return true;

        }catch (Exception e){
            log.error("删除文件失败，请重试:{}",e.getMessage(),e);
            return false;
        }
    }

    /**
     *获取FileKey
     * 1 判断bucket，并获取
     * 2 提取FileKey
     * 3 返回
     * @param fileUrl 文件URL
     * @return
     */
    @Override
    public String extractFileKey(String fileUrl) {
        if(StringUtils.isBlank(fileUrl)){
            log.info("Url为空，请重新输入");
            return null;
        }
        try{
            OssClientManager.BucketClient bucket = getBucketClient(fileUrl);
            if (bucket == null) {
                log.error("无法从Url中获取FileKey，请重新输入");
                return null;
            }
            String domainWithSlash = bucket.domainwithSlash();

            String result = fileUrl.startsWith(domainWithSlash)?fileUrl.substring(domainWithSlash.length()):null;

            try {
                result = URLDecoder.decode(result, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                log.error("fileKey URL解码失败：{}", result, e);
            }
            return result;
        }catch (Exception e){
            log.error("获取FileKey失败，请重试:{}",e.getMessage(),e);
            return null;
        }
    }

    /**
     * 生成预览的文件Url
     * 1 获取 bucket对象 并检验是否为null
     * 2 生成过期时间
     * 3 封装Oss请求
     * 4 返回生成的Url
     * @param fileUrl 文件URL
     * @return
     */
    @Override
    public String generatePreviewUrl(String fileUrl) {
        try{
            OssClientManager.BucketClient bucket = getBucketClient(fileUrl);

            if (bucket == null) {
                log.error("无法获取到对应的Url，请重新输入");
                return null;
            }

            if(bucket.isPublic()){
                return fileUrl;
            }

            String fileKey = extractFileKey(fileUrl);

            Date expires = new Date(System.currentTimeMillis()+PREVIEW_URL_EXPIRATION_MILLIS);

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket.getBucketName(), fileKey , HttpMethod.GET);
            request.setExpiration(expires);

            URL generatePresignedUrl = bucket.getClient().generatePresignedUrl(request);
            log.info("生成预览Url：{}，有效时长一小时",generatePresignedUrl.toString());
            return generatePresignedUrl.toString();
        }catch (Exception e){
            log.error("无法获取到对应的Url，请重试:{}",e.getMessage(),e);
            return null;
        }
    }


}
