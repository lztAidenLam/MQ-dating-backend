package com.lam.dating.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AidenLam
 * @date 2024/4/11
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("hello");
        return "springboot hello";
    }
}
