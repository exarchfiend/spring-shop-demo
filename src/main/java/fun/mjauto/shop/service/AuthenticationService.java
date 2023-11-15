package fun.mjauto.shop.service;

import fun.mjauto.shop.pojo.dto.ApiResponse;

/**
 * @author MJ
 * @description 验证码图片服务Captcha接口
 * @date 2023/11/14
 */
public interface AuthenticationService {
    ApiResponse<?> createCaptcha();
}
