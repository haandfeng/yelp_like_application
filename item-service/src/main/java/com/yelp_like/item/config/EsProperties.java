package com.yelp_like.item.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "yelp.es")
public class EsProperties {
    /**
     * 索引名称
     */
    private String index = "items";
    /**
     * Elasticsearch地址，支持多个，格式：http://host:port
     */
    private List<String> uris;
    private String username;
    private String password;
}

