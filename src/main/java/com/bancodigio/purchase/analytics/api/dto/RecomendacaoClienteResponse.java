package com.bancodigio.purchase.analytics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Recomendação de vinho personalizada para o cliente")
public record RecomendacaoClienteResponse(
        @Schema(description = "Nome do cliente", example = "João Silva")
        String nome,

        @Schema(description = "CPF do cliente", example = "12345678900")
        String cpf,

        @Schema(description = "Tipo de vinho recomendado", example = "Tinto")
        String tipoVinho,

        @Schema(description = "Preço do vinho recomendado", example = "150.00")
        BigDecimal preco,

        @Schema(description = "Safra do vinho recomendado", example = "2020")
        String safra
) {
}
