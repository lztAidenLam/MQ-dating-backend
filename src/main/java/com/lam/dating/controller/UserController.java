package com.lam.dating.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lam.dating.common.BaseResponse;
import com.lam.dating.common.ErrorCode;
import com.lam.dating.common.ResultUtils;
import com.lam.dating.exception.BusinessException;
import com.lam.dating.model.domain.request.UserLoginRequest;
import com.lam.dating.model.domain.request.UserRegisterRequest;
import com.lam.dating.model.entity.User;
import com.lam.dating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.lam.dating.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @author AidenLam
 * @date 2024/4/12
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;


    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {

        Page<User> userList = userService.getRecommendUsers(pageNum, pageSize, request);

        return ResultUtils.success(userList);
    }

    @PostMapping("/updateUser")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验数据
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 鉴权
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.updateUser(loginUser, user);
        return ResultUtils.success(result);
    }

    @PostMapping("/searchUsersByTags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestBody List<String> tagNameList) {
        List<User> usersByTags = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(usersByTags);
    }

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @GetMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.userLogout(request));
    }

    @GetMapping("/getAllUser")
    public BaseResponse<List<User>> getAllUser(HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return ResultUtils.success(userService.selectAll());
    }



    @PostMapping("/deleteOne")
    public BaseResponse<Boolean> deleteOne(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.removeById(id));
    }


    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        User safetyUser = userService.getSafetyUser(userService.getById(userId));
        return ResultUtils.success(safetyUser);
    }



}
