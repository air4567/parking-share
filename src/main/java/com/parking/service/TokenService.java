package com.parking.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String createToken(Long userId) {
        // token 本身不包含 "Bearer " 前缀，前端会在 header 中添加
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenToUserId.put(token, userId);
        return token;
    }

    public Long getUserIdByToken(String token) {
        if (token == null) return null;
        // 如果前端传入了 "Bearer xxxxx" 格式，去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return tokenToUserId.get(token);
    }

    public void removeToken(String token) {
        tokenToUserId.remove(token);
    }
}
