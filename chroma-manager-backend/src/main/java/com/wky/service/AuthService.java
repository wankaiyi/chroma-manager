package com.wky.service;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wky
 * @date 2025/09/03
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String JWT_SECRET = "chromadb-jwt-secret";

    public String createToken(String url, String tenant) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("url", url);
        payload.put("tenant", tenant);
        return JWTUtil.createToken(payload, JWT_SECRET.getBytes());
    }

    public boolean verifyToken(String token) {
        return JWTUtil.verify(token, JWT_SECRET.getBytes());
    }

    public Object getPayload(String token, String key) {
        JWT jwt = JWTUtil.parseToken(token);
        return jwt.getPayload(key);
    }

}
