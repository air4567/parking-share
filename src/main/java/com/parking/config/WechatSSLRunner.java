package com.parking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * 启动完成后再次设置 JVM 默认 HTTPS 信任，并打日志便于在云托管运行日志中确认。
 * 与 ParkingApplication.main 中的设置互为备份，确保云环境请求 api.weixin.qq.com 不报 SSL。
 */
@Slf4j
@Component
@Order(1)
public class WechatSSLRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] c, String a) { }
                        public void checkServerTrusted(X509Certificate[] c, String a) { }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
            log.info("WechatSSL: default HTTPS trust set for api.weixin.qq.com (login will use this)");
        } catch (Exception e) {
            log.error("WechatSSL: failed to set default SSL trust", e);
        }
    }
}
