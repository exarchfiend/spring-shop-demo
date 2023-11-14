package fun.mjauto.shop.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import fun.mjauto.shop.pojo.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static fun.mjauto.shop.constant.RedisConstants.*;

/**
 * @author MJ
 * @description
 * @date 2023/11/14
 */
@Slf4j
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    private final StringRedisTemplate stringRedisTemplate;

    public AuthenticationSuccessHandlerImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        response.addHeader("Authorization", token);
        // 将User对象转为Map存储
        UserDTO userDTO = BeanUtil.copyProperties(authentication.getPrincipal(), UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 保存用户信息到 redis中
        String tokenKey = AUTH_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, AUTH_USER_TTL, TimeUnit.MINUTES);
        // 打印登录日志
        log.info("用户：[{}]登陆成功,Token：{}", userDTO.getUsername(), token);
    }
}
