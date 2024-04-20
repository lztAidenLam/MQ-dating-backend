package com.lam.dating.constant;

/**
 * @author AidenLam
 * @date 2024/4/12
 */
public interface UserConstant {

    /**
     * 用户登录状态
     */
    String USER_LOGIN_STATUS = "userLoginStatus";

    /**
     * 管理员
     */
    Integer ADMIN_ROLE = 1;

    /**
     * 普通用户
     */
    Integer DEFAULT_ROLE = 0;

    /**
     * 已删除（逻辑）
     */
    String IS_DELETE = "1";

    /**
     * 未删除（逻辑）
     */
    String IS_NOT_DELETE = "0";
}
