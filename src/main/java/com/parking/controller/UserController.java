package com.parking.controller;

import com.parking.common.Result;
import com.parking.service.TokenService;
import com.parking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.trim().isEmpty()) return Result.fail("code不能为空");
        Map<String, Object> data = userService.login(code);
        if (data == null) return Result.fail("登录失败，请重试");
        return Result.ok(data);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Map<String, Object> data = userService.stats(userId);
        return Result.ok(data);
    }

    /**
     * 获取当前用户信息（用于刷新用户数据）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        Map<String, Object> userInfo = userService.getUserInfo(userId);
        if (userInfo == null) return Result.fail("用户不存在");
        return Result.ok(userInfo);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/info")
    public Result<Map<String, Object>> updateUserInfo(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Long userId = tokenService.getUserIdByToken(auth);
        if (userId == null) return Result.fail(401, "未登录");
        String nickname = body.get("nickname");
        String phone = body.get("phone");
        String avatar = body.get("avatar");
        Map<String, Object> userInfo = userService.updateUserInfo(userId, nickname, phone, avatar);
        if (userInfo == null) return Result.fail("用户不存在");
        return Result.ok(userInfo);
    }
}
