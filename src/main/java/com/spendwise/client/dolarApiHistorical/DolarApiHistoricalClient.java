package com.spendwise.client.dolarApiHistorical;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DolarApiHistoricalClient {

    private final RestClient restClient;

    public DolarApiHistoricalClient(
            @Qualifier("dolarApiHistoricalRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public DolarApiHistoricalDTO getRate(String type, String date) {
        return restClient.get()
                .uri("/v1/cotizaciones/dolares/{type}/{date}", type, date)
                .retrieve()
                .body(DolarApiHistoricalDTO.class);
    }
}
