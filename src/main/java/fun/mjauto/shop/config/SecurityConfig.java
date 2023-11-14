package fun.mjauto.shop.config;

import fun.mjauto.shop.filter.AuthenticationTokenFilter;
import fun.mjauto.shop.filter.LoggerRequestFilter;
import fun.mjauto.shop.filter.VerificationCodeFilter;
import fun.mjauto.shop.handler.AuthenticationFailureHandlerImpl;
import fun.mjauto.shop.handler.AuthenticationSuccessHandlerImpl;
import fun.mjauto.shop.handler.LogoutSuccessHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.DisableEncodeUrlFilter;

/**
 * @author MJ
 * @description Security配置类
 * @date 2023/11/14
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final StringRedisTemplate stringRedisTemplate;

    public SecurityConfig(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置访问权限：permitAll()拥有所有权限
        http.authorizeHttpRequests(authorizeHttpRequests->
                authorizeHttpRequests
                        .requestMatchers("/login").permitAll() // 获取验证码的URL不需要认证
                        .requestMatchers("/auth/code").permitAll() // 其它所有请求都需要认证，不可以匿名访问
                        .anyRequest().authenticated() // 其它所有请求都需要认证，不可以匿名访问
        );

        // 配置基于表单的登录认证
        http.formLogin(formLogin->
                formLogin
                        .loginProcessingUrl("/login") // 登录表单提交处理的URL
                        .successHandler(new AuthenticationSuccessHandlerImpl(stringRedisTemplate)) // 登录成功处理器
                        .failureHandler(new AuthenticationFailureHandlerImpl()) // 登录失败处理器
        );

        // 配置注销登录
        http.logout(logout->
                logout
                        .logoutUrl("/logout") // 注销登录的URL
                        .logoutSuccessHandler(new LogoutSuccessHandlerImpl()) // 退出成功处理器
        );

        // 配置自定义第一个过滤器
        http.addFilterBefore(new LoggerRequestFilter(), DisableEncodeUrlFilter.class);

        // 配置自定义验证码过滤器
        http.addFilterBefore(new VerificationCodeFilter(stringRedisTemplate), UsernamePasswordAuthenticationFilter.class);

        // 配置自定义JWT验证过滤器
        http.addFilterBefore(new AuthenticationTokenFilter(stringRedisTemplate), UsernamePasswordAuthenticationFilter.class);

        // 关闭跨域漏洞防御
        http.csrf(AbstractHttpConfigurer::disable);

        // 关闭跨域拦截
        http.cors(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 明文加密
        return NoOpPasswordEncoder.getInstance();
    }
}
