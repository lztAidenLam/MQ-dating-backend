package com.lam.dating.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author AidenLam
 * @date 2024/4/12
 */
@Data
public class UserRegisterRequest implements Serializable {

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private static final long serialVersionUID = -4246225931823422543L;
}
