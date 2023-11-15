package fun.mjauto.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.mjauto.shop.pojo.entity.UserDetailsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author MJ
 * @description 用户登录表访问接口
 * @date 2023/11/14
 */
@Mapper
public interface UserDetailsMapper extends BaseMapper<UserDetailsEntity> {
}
