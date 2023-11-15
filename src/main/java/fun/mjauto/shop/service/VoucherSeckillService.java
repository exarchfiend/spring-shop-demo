package fun.mjauto.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.entity.VoucherSeckillEntity;

/**
 * @author MJ
 * @description 秒杀优惠券服务接口
 * @date 2023/11/15
 */
public interface VoucherSeckillService extends IService<VoucherSeckillEntity> {
    ApiResponse<?> setVoucherSeckillCache(Long id);
}
