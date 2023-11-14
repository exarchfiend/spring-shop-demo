package fun.mjauto.shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author MJ
 * @description
 * @date 2023/11/14
 */
@Data
@AllArgsConstructor
public class CaptchaDTO {
    private String uuid; // 验证码标识
    private String base64Image; // 验证码图片
}
