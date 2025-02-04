package com.gdg.z_meet;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvTest {
    // 오류 해결 끝나면 지울 예정

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Value("${DEFAULT_SCHEMA}")
    private String db;

    @PostConstruct
    public void printEnvVariables() {
        System.out.println("✅ DB_USERNAME: " + dbUsername);
        System.out.println("✅ DB_PASSWORD: " + dbPassword);
        System.out.println("✅ DEFAULT_SCHEMA: " + db );
    }
}
