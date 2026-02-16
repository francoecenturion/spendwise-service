package com.spendwise.client.dolarApi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DolarApiClient {

    private final RestClient restClient;

    public DolarApiClient(
            @Qualifier("dolarApiRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public DolarApiDTO getRate(String type) {
        return restClient.get()
                .uri("/v1/dolares/{type}", type)
                .retrieve()
                .body(DolarApiDTO.class);
    }

}
