package fun.mjauto.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.entity.VoucherEntity;

/**
 * @author MJ
 * @description 优惠券服务接口
 * @date 2023/11/15
 */
public interface VoucherService extends IService<VoucherEntity> {
    ApiResponse<?> selectVoucherById(Long id);
}
