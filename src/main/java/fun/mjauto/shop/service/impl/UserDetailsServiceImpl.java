package fun.mjauto.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.mjauto.shop.mapper.UserDetailsMapper;
import fun.mjauto.shop.pojo.entity.UserDetailsEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author MJ
 * @description
 * @date 2023/11/14
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDetailsMapper userDetailsMapper;
    public UserDetailsServiceImpl(UserDetailsMapper userDetailsMapper) {
        this.userDetailsMapper = userDetailsMapper;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 根据username查询用户
        QueryWrapper<UserDetailsEntity> wrapperUser = new QueryWrapper<>();
        wrapperUser.eq("username", username); // 在查询条件中设置字段名和值
        UserDetailsEntity userDetails = userDetailsMapper.selectOne(wrapperUser); // 执行查询并返回结果
        // 判断查询结果是否为空
        if (userDetails == null) {
            throw new UsernameNotFoundException("未查询到此用户");
        }
        // 返回用户
        return userDetails;
    }
}
