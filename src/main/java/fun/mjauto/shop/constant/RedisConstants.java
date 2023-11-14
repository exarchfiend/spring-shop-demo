package fun.mjauto.shop.constant;

/**
 * @author MJ
 * @description Redis常量类
 * @date 2023/11/14
 */
public class RedisConstants {
    public static final String AUTH_CODE_KEY = "shop:auth:code:"; // 验证码 key
    public static final Long AUTH_CODE_TTL = 2L; // 验证码有效期
    public static final String AUTH_USER_KEY = "shop:auth:token:"; // Token key
    public static final Long AUTH_USER_TTL = 30L; // Token有效期
    public static final Long CACHE_NULL_TTL = 2L;
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_VOUCHER_TTL = 30L;
    public static final String CACHE_VOUCHER_KEY = "cache:voucher:";
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
