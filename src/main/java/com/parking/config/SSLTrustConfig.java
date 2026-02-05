package com.parking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * 启动时设置 JVM 默认 HTTPS 信任证书，解决云环境请求 api.weixin.qq.com 时 SSLHandshakeException。
 * 仅影响本应用进程内的出站 HTTPS 连接。
 */
@Slf4j
@Configuration
public class SSLTrustConfig {

    @PostConstruct
    public void trustHttpsForWechat() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            log.info("SSLTrustConfig: default HTTPS trust set for outbound connections (e.g. wechat api)");
        } catch (Exception e) {
            log.error("SSLTrustConfig: failed to set default SSL trust", e);
        }
    }
}
