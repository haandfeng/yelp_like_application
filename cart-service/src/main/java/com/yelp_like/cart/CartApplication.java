package com.yelp_like.cart;

import com.yelp_like.api.config.DefaultFeignConfig;
import com.yelp_like.cart.config.LoadBalancerConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@LoadBalancerClients(defaultConfiguration = LoadBalancerConfiguration.class)
@EnableFeignClients(basePackages = "com.yelp_like.api.client", defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("com.yelp_like.cart.mapper")
@SpringBootApplication
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}