package com.bancodigio.purchase.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record Produto(
        Integer codigo,
        @JsonProperty("tipo_vinho") String tipoVinho,
        BigDecimal preco,
        String safra,
        @JsonProperty("ano_compra") Integer anoCompra
) {}
