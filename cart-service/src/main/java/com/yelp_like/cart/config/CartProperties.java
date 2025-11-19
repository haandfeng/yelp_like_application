package com.yelp_like.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yelp.cart")
public class CartProperties {
    private Integer maxItems;
}
