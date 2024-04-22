package com.lam.dating.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author AidenLam
 * @date 2024/4/12
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1884830059531165077L;
    private String userAccount;
    private String userPassword;
}
