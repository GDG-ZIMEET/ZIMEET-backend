package com.gdg.z_meet.domain.order;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component       // 싱글톤으로 어디서든 사용 가능 할 수 있도록
@ConfigurationProperties(prefix = "payment")    // .yml 에서 "payment" 이하의 설정 값들을 읽어와 객체 필드에 매핑
public class PaymentProperties {
    private String secretKey;
    private String baseUrl;
    private String confirmEndpoint;

    public String getSecretKey() {return secretKey;}

    public String getBaseUrl() {return baseUrl;}

    public String getConfirmUrl() {return baseUrl + confirmEndpoint;}


    public void setSecretKey(final String secretKey) {this.secretKey = secretKey;}

    public void setBaseUrl(String baseUrl) {this.baseUrl = baseUrl;}

    public void setConfirmEndpoint(String confirmEndpoint) {this.confirmEndpoint = confirmEndpoint;}
}
