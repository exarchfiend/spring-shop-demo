package fun.mjauto.shop.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author MJ
 * @description 基础实体类
 * @date 2023/11/15
 */
@Data
public class BaseEntity implements Serializable {
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
