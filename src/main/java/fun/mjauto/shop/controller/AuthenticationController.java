package fun.mjauto.shop.controller;

import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.service.AuthenticationService;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

/**
 * @author MJ
 * @description 认证API
 * @date 2023/11/14
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @SneakyThrows
    @GetMapping("/code")
    public ApiResponse<?> getCode() {
        return authenticationService.createCaptcha();
    }
}
