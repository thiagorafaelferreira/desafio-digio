package com.bancodigio.purchase.analytics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Detalhes completos de uma compra realizada")
public record CompraResponse(
        @Schema(description = "Nome do cliente que realizou a compra", example = "João Silva")
        String clienteNome,

        @Schema(description = "CPF do cliente", example = "12345678900")
        String clienteCpf,

        @Schema(description = "Código do produto comprado", example = "1001")
        Integer produtoCodigo,

        @Schema(description = "Tipo do vinho comprado", example = "Tinto")
        String produtoTipo,

        @Schema(description = "Safra do vinho", example = "2020")
        String produtoSafra,

        @Schema(description = "Ano em que a compra foi realizada", example = "2023")
        Integer produtoAnoCompra,

        @Schema(description = "Preço unitário do produto", example = "150.00")
        BigDecimal precoUnitario,

        @Schema(description = "Quantidade de unidades compradas", example = "2")
        Integer quantidade,

        @Schema(description = "Valor total da compra (preço unitário x quantidade)", example = "300.00")
        BigDecimal valorTotal
) {
}
