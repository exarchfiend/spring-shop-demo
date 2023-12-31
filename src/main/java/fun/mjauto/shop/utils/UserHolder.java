package fun.mjauto.shop.utils;

import fun.mjauto.shop.pojo.dto.UserDTO;

/**
 * @author MJ
 * @description 线程操作用户
 * @date 2023/11/14
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
