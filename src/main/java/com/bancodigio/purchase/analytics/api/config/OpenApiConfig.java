package com.bancodigio.purchase.analytics.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig
 * Define the basic information for the doc api
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Compras e Recomendação de Vinhos")
                        .description("Endpoints para listagem de compras, maiores clientes e recomendações")
                        .version("v1.0.0"));
    }
}
