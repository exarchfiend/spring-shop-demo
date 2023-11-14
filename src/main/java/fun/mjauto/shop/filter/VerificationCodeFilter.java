package fun.mjauto.shop.filter;

import cn.hutool.json.JSONUtil;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static fun.mjauto.shop.constant.RedisConstants.AUTH_CODE_KEY;

/**
 * @author MJ
 * @description 验证码过滤器
 * @date 2023/11/14
 */
public class VerificationCodeFilter extends OncePerRequestFilter {
    private final StringRedisTemplate stringRedisTemplate;

    public VerificationCodeFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 使用AntPathRequestMatcher匹配路径
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/login", "POST");

        // 判断当前请求是否匹配路径和方法
        if (!matcher.matches(request)) {
            // 验证通过，后面会开始用户名和密码验证
            filterChain.doFilter(request, response);
            return;
        }

        // 在这里编写验证码验证逻辑
        if (!isCaptchaValid(request)) {
            // 验证失败
            response.setContentType("application/json"); // 设置响应内容类型为JSON
            response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
            // 返回错误信息
            response.getWriter().write(JSONUtil.toJsonStr(new ApiResponse<>().fail("验证码错误")));
        } else {
            // 验证通过，后面会开始用户名和密码验证
            filterChain.doFilter(request, response);
        }

    }

    private boolean isCaptchaValid(HttpServletRequest request) {
        // 拿到输入的验证码
        String code = request.getParameter("code");
        // 拿到验证码标识
        String uuid = request.getParameter("uuid");
        // 拿到缓存中的验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(AUTH_CODE_KEY + uuid);
        // 输入的验证码为空
        if (code == null) {
            // 验证失败
            return false;
        }
        // 验证是否相等
        return code.equals(cacheCode);
    }
}
