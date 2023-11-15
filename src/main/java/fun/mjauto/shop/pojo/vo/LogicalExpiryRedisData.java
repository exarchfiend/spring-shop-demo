package fun.mjauto.shop.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author MJ
 * @description 逻辑过期时间包装类
 * @date 2023/11/15
 */
@Data
public class LogicalExpiryRedisData {
    private LocalDateTime ExpireTime;
    private Object data;
}
