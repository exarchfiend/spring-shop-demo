package fun.mjauto.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.mjauto.shop.pojo.entity.VoucherEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MJ
 * @description 优惠券表访问接口
 * @date 2023/11/15
 */
@Mapper
public interface VoucherMapper extends BaseMapper<VoucherEntity> {
}
