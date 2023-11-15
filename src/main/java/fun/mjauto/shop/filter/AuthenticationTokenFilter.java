package fun.mjauto.shop.filter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.dto.UserDTO;
import fun.mjauto.shop.utils.UserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static fun.mjauto.shop.constant.RedisConstants.*;

/**
 * @author MJ
 * @description Token过滤器
 * @date 2023/11/14
 */
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final StringRedisTemplate stringRedisTemplate;

    public AuthenticationTokenFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 使用AntPathRequestMatcher匹配路径
        AntPathRequestMatcher loginMatcher = new AntPathRequestMatcher("/login", "POST"); // 登录路径
        // AntPathRequestMatcher loginMatcher = new AntPathRequestMatcher("/login"); // 登录路径
        AntPathRequestMatcher authCodeMatcher = new AntPathRequestMatcher("/auth/code", "GET"); // 获取验证码路径

        // 排除掉登录请求和获取验证码请求
        if (loginMatcher.matches(request) || authCodeMatcher.matches(request)) {
            // 继续执行过滤器链，不执行后面的逻辑
            filterChain.doFilter(request, response);
            return;
        }

        // 在这里执行刷新Token逻辑
        if (isTokenExpire(request)) {
            response.setContentType("application/json"); // 设置响应内容类型为JSON
            response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
            // 返回错误信息
            response.getWriter().write(JSONUtil.toJsonStr(new ApiResponse<>().fail("Token过期")));
        } else {
            // Token没有过期且已经刷新有效期
            filterChain.doFilter(request, response);
        }
    }

    private boolean isTokenExpire(HttpServletRequest request) {
        // 获取请求头中的token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 2.基于TOKEN获取redis中的用户
        String key  = AUTH_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 3.判断用户是否存在
        if (userMap.isEmpty()) {
            return true;
        }
        // 5.将查询到的hash数据转为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 6.存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        // 5.刷新token有效期
        stringRedisTemplate.expire(key, AUTH_USER_TTL, TimeUnit.MINUTES);
        // 6.放行
        return false;
    }
}
