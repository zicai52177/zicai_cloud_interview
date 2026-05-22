package net.zicai.constant;

/**
 * 缓存相关常量
 */
public class CacheConstants {

    /**
     * 套餐缓存Key前缀
     */
    public static final String PRODUCT_PACKAGE_KEY_PREFIX = "product:packages:";

    /**
     * 套餐列表缓存Key
     */
    public static final String PRODUCT_PACKAGE_LIST_KEY = PRODUCT_PACKAGE_KEY_PREFIX + "list";

    /**
     * 套餐缓存版本（数据变更时更新）
     */
    public static final String PRODUCT_PACKAGE_VERSION_KEY = PRODUCT_PACKAGE_KEY_PREFIX + "version";

    /**
     * 套餐列表缓存的SET NX key（用于缓存击穿保护）
     */
    public static final String PRODUCT_PACKAGE_LIST_LOCK_KEY = PRODUCT_PACKAGE_KEY_PREFIX + "list:lock";

    /**
     * 套餐列表缓存TTL（30分钟）
     */
    public static final long PRODUCT_PACKAGE_LIST_TTL_SECONDS = 30 * 60;

    /**
     * 空值缓存TTL（防止缓存穿透，5分钟）
     */
    public static final long NULL_VALUE_TTL_SECONDS = 5 * 60;
}