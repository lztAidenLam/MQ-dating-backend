package com.lam.dating.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author AidenLam
 * @date 2024/4/22
 */

@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 6063606793405364943L;

    protected int pageSize = 10;

    protected int pagNum = 1;
}
