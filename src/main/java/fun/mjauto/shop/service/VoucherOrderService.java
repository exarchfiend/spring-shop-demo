package fun.mjauto.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.entity.VoucherOrderEntity;

/**
 * @author MJ
 * @description 优惠券订单服务接口
 * @date 2023/11/15
 */
public interface VoucherOrderService extends IService<VoucherOrderEntity> {
    ApiResponse<?> createVoucherOrder(Long id);
    boolean createVoucherOrder(VoucherOrderEntity voucherOrder);
}
