package fun.mjauto.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.mjauto.shop.pojo.entity.VoucherOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MJ
 * @description 优惠券订单表访问接口
 * @date 2023/11/15
 */
@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrderEntity> {
}
