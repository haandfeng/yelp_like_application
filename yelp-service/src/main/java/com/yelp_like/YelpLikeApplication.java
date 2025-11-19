package com.yelp_like;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.yelp_like.mapper")
@SpringBootApplication
public class YelpLikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(YelpLikeApplication.class, args);
    }
}