package fun.mjauto.shop.service.impl;

import cn.hutool.core.lang.UUID;
import com.google.code.kaptcha.Producer;
import fun.mjauto.shop.pojo.dto.ApiResponse;
import fun.mjauto.shop.pojo.dto.CaptchaDTO;
import fun.mjauto.shop.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static fun.mjauto.shop.constant.RedisConstants.*;

/**
 * @author MJ
 * @description 验证码图片服务Kaptcha实现
 * @date 2023/11/14
 */
@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final Producer producer; // 验证码生成工具配置
    private final StringRedisTemplate stringRedisTemplate;

    public AuthenticationServiceImpl(Producer producer, StringRedisTemplate stringRedisTemplate) {
        this.producer = producer;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ApiResponse<?> createCaptcha() {
        // 生成一个随机的验证码字符串
        String code = producer.createText();
        // 生成UUID标识验证码
        String uuid = UUID.randomUUID().toString(true);
        // 将生成的验证码字符串存储到redis中
        stringRedisTemplate.opsForValue().set(AUTH_CODE_KEY + uuid, code, AUTH_CODE_TTL, TimeUnit.MINUTES);
        // 创建一个包含验证码图像的BufferedImage对象
        BufferedImage image = producer.createImage(code);
        // 处理图片格式 <img src="data:image/jpg;base64, ${base64Image}" alt="Generated Image">
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            return new ApiResponse<>().fail("生成验证码失败");
        }
        String base64Image = Base64.getEncoder().encodeToString(out.toByteArray());
        // 将生成的验证码字符串记录到日志中
        log.info("验证码发送成功，验证码：{}，标识：{}", code, uuid);
        // 返回验证码标识和图片
        return new ApiResponse<>().success(new CaptchaDTO(uuid, base64Image));
    }
}
