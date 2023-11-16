package fun.mjauto.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.shop.manager.CacheClient;
import fun.mjauto.shop.mapper.VoucherMapper;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.entity.VoucherEntity;
import fun.mjauto.shop.pojo.entity.VoucherSeckillEntity;
import fun.mjauto.shop.service.VoucherSeckillService;
import fun.mjauto.shop.service.VoucherService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static fun.mjauto.shop.constant.RedisConstants.*;

/**
 * @author MJ
 * @description 优惠券服务接口实现
 * @date 2023/11/15
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, VoucherEntity> implements VoucherService {
    private final CacheClient cacheClient;
    private final VoucherSeckillService voucherSeckillService;

    public VoucherServiceImpl(CacheClient cacheClient, VoucherSeckillService voucherSeckillService) {
        this.cacheClient = cacheClient;
        this.voucherSeckillService = voucherSeckillService;
    }

    @Override
    public ApiResponse<?> selectVoucherById(Long id) {
        // 缓存穿透 是指客户端请求的数据在缓存中和数据库中都不存在 这样缓存永远不会生效 这些请求都会打到数据库
        // 缓存空对象解决缓存穿透
        // VoucherEntity voucher = cacheClient
        //         .queryWithPassThrough(CACHE_VOUCHER_KEY, id, VoucherEntity.class, this::getById, CACHE_VOUCHER_TTL, TimeUnit.MINUTES);

        // 缓存击穿 就是一个被高并发访问并且缓存重建业务较复杂的key突然失效了 无数的请求访问会在瞬间给数据库带来巨大的冲击
        // 1.互斥锁解决缓存击穿
        VoucherEntity voucher = cacheClient
                .queryWithMutex(CACHE_VOUCHER_KEY, id, VoucherEntity.class, this::getVoucherById, CACHE_VOUCHER_TTL, TimeUnit.MINUTES);

        // 2.逻辑过期解决缓存击穿  测试 10L TimeUnit.SECONDS
        // VoucherEntity voucher = cacheClient
        //         .queryWithLogicalExpire(CACHE_VOUCHER_KEY, id, VoucherEntity.class, this::getById, CACHE_VOUCHER_TTL, TimeUnit.MINUTES);

        if (voucher == null) {
            return new ApiResponse<>().fail("优惠券不存在");
        }
        // 返回优惠券响应
        return new ApiResponse<>().success(voucher);
    }

    public VoucherEntity getVoucherById(Long id) {
        // 查询优惠券
        VoucherEntity voucher = this.getById(id);
        // 判断优惠券是否为空
        if (voucher == null) {
            return null;
        }
        // 判断优惠券类型是否为秒杀
        if (voucher.getType() == 2) {
            // 秒杀券需要查询秒杀券表
            VoucherSeckillEntity seckillVoucher = voucherSeckillService.getById(id);
            // 设置库存和有效期
            voucher.setStock(seckillVoucher.getStock())
                    .setBeginTime(seckillVoucher.getBeginTime())
                    .setEndTime(seckillVoucher.getEndTime());
        }
        // 返回优惠券
        return voucher;
    }
}
