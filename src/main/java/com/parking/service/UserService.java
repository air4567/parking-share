package com.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final WechatService wechatService;
    private final TokenService tokenService;

    public Map<String, Object> login(String code) {
        String openid = wechatService.getOpenid(code);
        if (openid == null) return null;

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setRole("user");  // 普通用户
            user.setStatus(1);
            userMapper.insert(user);
        }

        String token = tokenService.createToken(user.getId());
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return result;
    }

    public Map<String, Object> stats(Long userId) {
        Map<String, Object> m = new HashMap<>();
        m.put("publishedCount", 0);
        m.put("orderCount", 0);
        m.put("completedCount", 0);
        return m;
    }

    /**
     * 获取用户信息（用于刷新用户数据）
     * @param userId 用户ID
     * @return 用户信息
     */
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        return userInfo;
    }

    /**
     * 更新用户信息（昵称/电话/头像等）
     */
    public Map<String, Object> updateUserInfo(Long userId, String nickname, String phone, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;

        if (nickname != null) {
            String n = nickname.trim();
            if (!n.isEmpty()) user.setNickname(n);
        }

        if (phone != null) {
            String p = phone.trim();
            if (p.isEmpty()) user.setPhone(null);
            else user.setPhone(p);
        }

        if (avatar != null) {
            String a = avatar.trim();
            if (a.isEmpty()) user.setAvatar(null);
            else user.setAvatar(a);
        }

        userMapper.updateById(user);
        return getUserInfo(userId);
    }
}
