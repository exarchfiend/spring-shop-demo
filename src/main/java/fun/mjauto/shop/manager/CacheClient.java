package fun.mjauto.shop.manager;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author MJ
 * @description 缓存服务接口类
 * @date 2023/11/15
 */
public interface CacheClient {
    /**
     * 缓存空对象解决缓存穿透
     *
     * @param keyPrefix  redis的key字段
     * @param id         实体类id。
     * @param type       实体类类型。
     * @param dbFallback 查询数据库函数。
     * @param time       过期时间数值。
     * @param unit       过期时间单位。
     * @return 查询到的实体类
     */
    <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit);
    /**
     * 互斥锁解决缓存击穿
     *
     * @param keyPrefix  redis的key字段
     * @param id         实体类id。
     * @param type       实体类类型。
     * @param dbFallback 查询数据库函数。
     * @param time       过期时间数值。
     * @param unit       过期时间单位。
     * @return 查询到的实体类
     */
    <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit);
    /**
     * 逻辑过期解决缓存击穿
     *
     * @param keyPrefix  redis的key字段
     * @param id         实体类id。
     * @param type       实体类类型。
     * @param dbFallback 查询数据库函数。
     * @param time       过期时间数值。
     * @param unit       过期时间单位。
     * @return 查询到的实体类
     */
    <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit);
    /**
     * 生成全局唯一ID
     *
     * @param keyPrefix  redis的key字段
     * @return 全局唯一ID
     */
    long nextGlobalUniqueId(String keyPrefix);
}
