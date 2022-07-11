package com.github.qyl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RedisRateLimitApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisRateLimitApplication.class, args);
    }



}
