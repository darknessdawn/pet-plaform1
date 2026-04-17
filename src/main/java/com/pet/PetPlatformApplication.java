package com.pet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class
})
@MapperScan("com.pet.mapper")
@EnableCaching
public class PetPlatformApplication {

    public static void main(String[] args) {
        // 先启动 Spring 容器
        var context = SpringApplication.run(PetPlatformApplication.class, args);
        // 从 Environment 获取实际端口
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8082");
        String contextPath = env.getProperty("server.servlet.context-path", "/api");

        System.out.println("========================================");
        System.out.println("   宠物用品交易平台启动成功!");
        System.out.println("   访问地址: http://localhost:" + port + contextPath);
        System.out.println("   API文档: http://localhost:" + port + contextPath + "/swagger-ui.html");
        System.out.println("========================================");
    }
}