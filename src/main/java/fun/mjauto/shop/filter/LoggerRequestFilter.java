package fun.mjauto.shop.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author MJ
 * @description
 * @date 2023/11/14
 */
public class LoggerRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 拦截所有请求，打印访问日志
        String url = request.getRequestURI();
        String method = request.getMethod();
        logger.info("地址：" + url + " 方法：" + method);
        filterChain.doFilter(request,response);
    }
}
