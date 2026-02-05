package com.parking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@SpringBootApplication
@MapperScan("com.parking.mapper")
public class ParkingApplication {

    public static void main(String[] args) {
        // 最早设置：解决云环境请求 api.weixin.qq.com 时 SSLHandshakeException（必须在 Spring 启动前）
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
            System.out.println("[ParkingApplication] default SSL trust set for wechat api");
        } catch (Exception e) {
            System.err.println("WARN: could not set default SSL trust: " + e.getMessage());
        }
        SpringApplication.run(ParkingApplication.class, args);
    }
}
