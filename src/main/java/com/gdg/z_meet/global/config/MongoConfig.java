package com.gdg.z_meet.global.config;

import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// MongoDB 리포지토리만 활성화
@Configuration
@EnableMongoRepositories(basePackages = "com.gdg.z_meet.domain.chat.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "zmeet"; // 사용할 MongoDB 데이터베이스 이름
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017"); // MongoDB 서버 주소
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
