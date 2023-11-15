package fun.mjauto.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.mjauto.shop.pojo.entity.VoucherSeckillEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MJ
 * @description 秒杀优惠券表访问接口
 * @date 2023/11/15
 */
@Mapper
public interface VoucherSeckillMapper extends BaseMapper<VoucherSeckillEntity> {
}
