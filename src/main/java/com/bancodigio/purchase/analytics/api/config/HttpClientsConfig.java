package com.bancodigio.purchase.analytics.api.config;

import com.bancodigio.purchase.analytics.api.client.VercelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * HttpClientsConfig
 * Define the rest client configuration for apis
 */
@Configuration
public class HttpClientsConfig {

    @Value("${external.base.url.vercel}")
    private String baseUrlVercel;

    @Bean
    RestClient vercelRestClient(RestClient.Builder builder) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        return builder
                .baseUrl(baseUrlVercel)
                .requestFactory(factory)
                .build();
    }

    @Bean
    VercelClient vercelClient(RestClient vercelRestClient) {
        var factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(vercelRestClient))
                .build();

        return factory.createClient(VercelClient.class);
    }
}
