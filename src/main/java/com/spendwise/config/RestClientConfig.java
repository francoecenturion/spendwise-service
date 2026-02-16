package com.spendwise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("dolarApiRestClient")
    public RestClient dolarApi(
            @Value("${external.dolarApi.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("dolarApiHistoricalRestClient")
    public RestClient dolarApiHistorical(
            @Value("${external.dolarApiHistorical.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

}

