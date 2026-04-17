package com.pet.controller;

import com.pet.common.Result;
import com.pet.dto.PasswordChangeDTO;
import com.pet.dto.UserLoginDTO;
import com.pet.dto.UserRegisterDTO;
import com.pet.dto.UserUpdateDTO;
import com.pet.service.UserService;
import com.pet.vo.LoginVO;
import com.pet.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        LoginVO loginVO = userService.register(registerDTO);
        return Result.success(loginVO);
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }
    
    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(@RequestBody UserUpdateDTO updateDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserVO userVO = userService.updateUserInfo(userId, updateDTO);
        return Result.success(userVO);
    }
    
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDTO passwordDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, passwordDTO);
        return Result.success();
    }
}
