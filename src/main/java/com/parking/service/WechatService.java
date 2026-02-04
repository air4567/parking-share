package com.parking.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.parking.config.WechatConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {

    private final WechatConfig wechatConfig;
    private final RestTemplate restTemplate;

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    public String getOpenid(String code) {
        String url = String.format(CODE2SESSION_URL, wechatConfig.getAppid(), wechatConfig.getSecret(), code);
        try {
            String resp = restTemplate.getForObject(url, String.class);
            JSONObject obj = JSON.parseObject(resp);
            if (obj.containsKey("errcode") && obj.getIntValue("errcode") != 0) {
                log.warn("wechat code2session err: {}", resp);
                return null;
            }
            return obj.getString("openid");
        } catch (Exception e) {
            log.error("wechat code2session error", e);
            return null;
        }
    }
}
