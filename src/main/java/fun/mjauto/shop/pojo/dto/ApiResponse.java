package fun.mjauto.shop.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author MJ
 * @description
 * @date 2023/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public ApiResponse<?> success(Object object){
        return new ApiResponse<>(200,"成功",object);
    }

    public ApiResponse<?> fail(String msg){
        return new ApiResponse<>(500,msg,null);
    }
}
