package fun.mjauto.shop.manager.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import fun.mjauto.shop.manager.CacheClient;
import fun.mjauto.shop.pojo.vo.LogicalExpiryRedisData;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static fun.mjauto.shop.constant.RedisConstants.*;

/**
 * @author MJ
 * @description 缓存服务Redis实现类
 * @date 2023/11/15
 */
@Service
public class RedisClientImpl implements CacheClient {
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    public RedisClientImpl(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(json, type);
        }

        // 通过缓存空对象解决缓存穿透
        // 判断命中的是否是空值
        if (json != null) {
            // 返回一个错误信息
            return null;
        }

        // 4.不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        // 5.不存在，返回错误
        if (r == null) {
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 6.存在，写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), time, unit);
        return r;
    }

    @Override
    public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        // 判断命中的是否是空值 通过缓存空对象解决缓存穿透的步骤
        if (json != null) {
            // 返回一个错误信息
            return null;
        }

        // 4.实现缓存重建
        // 4.1.获取互斥锁
        String lockKey = key + "lock:" + id;
        R r = null;
        // 创建锁对象
        RLock lock =  redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁
            boolean isLock = lock.tryLock();
            // 4.2.判断是否获取成功
            if (!isLock) {
                // 4.3.获取锁失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            // 4.4.获取锁成功，根据id查询数据库
            r = dbFallback.apply(id);
            // 5.不存在，返回错误
            if (r == null) {
                // 将空值写入redis 通过缓存空对象解决缓存穿透的步骤
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }
            // 6.存在，写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7.释放锁
            lock.unlock();
        }
        // 8.返回
        return r;
    }

    @Override
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在(逻辑过期的缓存是需要单独进行初始化的 不然数据库中有也会被redis返回null)
        if (StrUtil.isBlank(json)) {
            // 3.不存在，直接返回
            return null;
        }
        // 4.命中，需要先把json反序列化为对象
        LogicalExpiryRedisData redisData1 = JSONUtil.toBean(json, LogicalExpiryRedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData1.getData(), type);
        LocalDateTime expireTime = redisData1.getExpireTime();
        // 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1.未过期，直接返回店铺信息
            return r;
        }
        // 5.2.已过期，需要缓存重建
        // 6.缓存重建
        // 6.1.获取互斥锁
        String lockKey = key + "lock:" + id;
        // 创建锁对象
        RLock lock =  redissonClient.getLock(lockKey);
        // 尝试获取锁
        boolean isLock = lock.tryLock();
        // 6.2.判断是否获取锁成功
        if (isLock) {
            // 6.3.成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newR = dbFallback.apply(id);
                    // 重建缓存
                    // 设置逻辑过期
                    LogicalExpiryRedisData redisData2 = new LogicalExpiryRedisData();
                    redisData2.setData(newR);
                    redisData2.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
                    // 写入Redis
                    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData2));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            });
        }
        // 6.4.返回过期的商铺信息
        return r;
    }

    @Override
    public long nextGlobalUniqueId(String keyPrefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - ID_BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1.获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2.判断是否有空的key
        if (StrUtil.isBlank(keyPrefix) || StrUtil.isBlank(date)) {
            throw new RuntimeException("生成全局唯一ID失败");
        }
        // 2.3.获取互斥锁
        String lockKey = "id";
        long count = 0;
        // 创建锁对象
        RLock lock =  redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁
            boolean isLock = lock.tryLock();
            // 2.4.判断是否获取锁成功
            if (!isLock) {
                // 2.5.获取锁失败，休眠并重试
                Thread.sleep(50);
                return nextGlobalUniqueId(keyPrefix);
            }
            // 2.6.生成序列号
            Long incrementResult = stringRedisTemplate.opsForValue().increment(GLOBAL_ID_KEY + keyPrefix + ":" + date);
            // 检查结果是否为空值
            if (incrementResult == null){
                throw new RuntimeException("生成全局唯一ID失败");
            }
            count = incrementResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 2.7.释放锁
            lock.unlock();
        }
        // 3.拼接并返回
        return timestamp << ID_COUNT_BITS | count;
    }
}
