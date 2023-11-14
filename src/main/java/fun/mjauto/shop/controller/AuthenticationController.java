package fun.mjauto.shop.controller;

import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.service.AuthenticationService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * @author MJ
 * @description 验证码图片API
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

    @GetMapping ("/index")
    public ApiResponse<?> index() {
        return authenticationService.createCaptcha();
    }
}
