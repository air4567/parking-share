package com.parking.util;

import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 权限检查工具类
 */
@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserMapper userMapper;

    /**
     * 检查用户是否为管理员
     * @param userId 用户ID
     * @return true 如果是管理员
     */
    public boolean isAdmin(Long userId) {
        if (userId == null) return false;
        User user = userMapper.selectById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 角色字符串，如果用户不存在返回 null
     */
    public String getUserRole(Long userId) {
        if (userId == null) return null;
        User user = userMapper.selectById(userId);
        return user != null ? user.getRole() : null;
    }
}
