package com.yelp_like.item.config;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(EsProperties.class)
@RequiredArgsConstructor
public class ElasticsearchClientConfig {

    private final EsProperties esProperties;

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient() {
        List<String> uris = esProperties.getUris();
        if (CollectionUtils.isEmpty(uris)) {
            throw new IllegalStateException("yelp.es.uris 配置不能为空");
        }
        HttpHost[] hosts = uris.stream().map(HttpHost::create).collect(Collectors.toList()).toArray(new HttpHost[0]);
        RestClientBuilder builder = RestClient.builder(hosts);
        if (esProperties.getUsername() != null) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(esProperties.getUsername(), esProperties.getPassword()));
            builder.setHttpClientConfigCallback((RestClientBuilder.HttpClientConfigCallback) httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        return new RestHighLevelClient(builder);
    }
}

