package fun.mjauto.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.mjauto.shop.mapper.VoucherSeckillMapper;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.entity.VoucherSeckillEntity;
import fun.mjauto.shop.service.VoucherSeckillService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static fun.mjauto.shop.constant.RedisConstants.SECKILL_STOCK_KEY;

/**
 * @author MJ
 * @description 秒杀优惠券服务接口实现
 * @date 2023/11/15
 */
@Service
public class VoucherSeckillServiceImpl extends ServiceImpl<VoucherSeckillMapper, VoucherSeckillEntity> implements VoucherSeckillService {
    private final StringRedisTemplate stringRedisTemplate;

    public VoucherSeckillServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ApiResponse<?> setVoucherSeckillCache(Long id) {
        VoucherSeckillEntity voucherSeckill = getById(id);
        if (voucherSeckill == null) {
            return new ApiResponse<>().fail("秒杀优惠券不存在");
        }
        Map<String, String> voucherSeckillMap = new HashMap<>();
        voucherSeckillMap.put("stock", String.valueOf(Long.valueOf(voucherSeckill.getStock())));
        voucherSeckillMap.put("beginTime", String.valueOf(Timestamp.valueOf(voucherSeckill.getBeginTime()).getTime()));
        voucherSeckillMap.put("endTime", String.valueOf(Timestamp.valueOf(voucherSeckill.getEndTime()).getTime()));
        stringRedisTemplate.opsForHash().putAll(SECKILL_STOCK_KEY + id, voucherSeckillMap);
        return new ApiResponse<>().success(voucherSeckill);
    }
}
