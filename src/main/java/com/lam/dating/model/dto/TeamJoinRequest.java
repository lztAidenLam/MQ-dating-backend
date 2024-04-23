package com.lam.dating.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户添加队伍请求体
 *
 * @author AidenLam
 */
@Data
public class TeamJoinRequest implements Serializable {


    private static final long serialVersionUID = 4738794462398129610L;


    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}