package fun.mjauto.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.shop.manager.CacheClient;
import fun.mjauto.shop.mapper.VoucherOrderMapper;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.entity.VoucherOrderEntity;
import fun.mjauto.shop.service.VoucherOrderService;
import fun.mjauto.shop.service.VoucherSeckillService;
import fun.mjauto.shop.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author MJ
 * @description 优惠券订单服务接口实现
 * @date 2023/11/15
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrderEntity> implements VoucherOrderService {
    private final CacheClient cacheClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherSeckillService voucherSeckillService;
    private final RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    public VoucherOrderServiceImpl(CacheClient cacheClient, StringRedisTemplate stringRedisTemplate, VoucherSeckillService voucherSeckillService, RedissonClient redissonClient) {
        this.cacheClient = cacheClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.voucherSeckillService = voucherSeckillService;
        this.redissonClient = redissonClient;
    }

    @Override
    public ApiResponse<?> createVoucherOrder(Long id) {
        Long userId = UserHolder.getUser().getId();
        long orderId = cacheClient.nextGlobalUniqueId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                id.toString(), String.valueOf(userId), String.valueOf(orderId), String.valueOf(System.currentTimeMillis())
        );
        // result不能为null
        if (result == null) {
            return new ApiResponse<>().fail("执行lua脚本出错");
        }
        // 2.判断订单消息是否发布成功 结果为0说明成功
        return switch (result.intValue()) {
            case 0 -> new ApiResponse<>().success(orderId);
            case 1 -> new ApiResponse<>().fail("活动尚未开始");
            case 2 -> new ApiResponse<>().fail("活动已经结束");
            case 3 -> new ApiResponse<>().fail("库存不足");
            case 4 -> new ApiResponse<>().fail("不能重复下单");
            default -> new ApiResponse<>().fail("系统错误");
        };
    }

    @Override
    public boolean createVoucherOrder(VoucherOrderEntity voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // 创建锁对象
        RLock lock = redissonClient.getLock("lock:order" + userId);
        // 尝试获取锁
        boolean isLock = lock.tryLock();
        // 判断
        if (!isLock) {
            // 获取锁失败，直接返回失败或者重试
            log.info("不能重复下单");
            return false;
        }

        try {
            // 根据优惠券id和用户id查询订单信息
            long count = query()
                    .eq("voucher_id", voucherId)
                    .eq("user_id", userId)
                    .count();
            // 判断用户是否已经有订单
            if (count > 0) {
                log.info("不能重复下单");
                return false;
            }
            // 扣减库存
            boolean success = voucherSeckillService.update()
                    .setSql("stock = stock - 1") // set stock = stock - 1
                    .eq("voucher_id", voucherId) // where id = ?
                    // .eq("stock",seckillVoucher.getStock()) // 乐观锁存在卖不完的问题
                    .gt("stock", 0) // and stock > 0 // 修改条件
                    .update();
            if (!success) {
                // 库存不足
                log.info("库存不足");
                return false;
            }
            // 创建订单
            log.info("秒杀成功");
            return save(voucherOrder);
        } finally {
            lock.unlock();
        }
    }
}
