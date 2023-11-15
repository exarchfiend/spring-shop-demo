package fun.mjauto.shop.constant;

/**
 * @author MJ
 * @description Redis常量类
 * @date 2023/11/14
 */
public class RedisConstants {
    public static final String GLOBAL_ID_KEY = "global:id:"; // 全局唯一ID开始时间戳
    public static final long ID_BEGIN_TIMESTAMP = 1699300690L; // 全局唯一ID开始时间戳
    public static final int ID_COUNT_BITS = 32; //  全局唯一ID序列号的位数
    public static final String AUTH_CODE_KEY = "shop:auth:code:"; // 验证码 key
    public static final Long AUTH_CODE_TTL = 2L; // 验证码有效期
    public static final String AUTH_USER_KEY = "shop:auth:token:"; // Token key
    public static final Long AUTH_USER_TTL = 30L; // Token有效期
    public static final Long CACHE_NULL_TTL = 2L;  // 缓存穿透有效期
    public static final String CACHE_VOUCHER_KEY = "shop:cache:voucher:"; // 优惠券 key
    public static final Long CACHE_VOUCHER_TTL = 30L; // 优惠券有效期
    public static final String SECKILL_STOCK_KEY = "shop:seckill:stock:";  // 秒杀优惠券库存 key
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "shop:cache:shop:";
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
}
