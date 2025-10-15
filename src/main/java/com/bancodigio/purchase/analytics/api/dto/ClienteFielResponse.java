package com.bancodigio.purchase.analytics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Informações de cliente fiel com histórico de compras")
public record ClienteFielResponse(
        @Schema(description = "CPF do cliente", example = "12345678900")
        String cpf,

        @Schema(description = "Nome completo do cliente", example = "João Silva")
        String nome,

        @Schema(description = "Valor total gasto pelo cliente", example = "5000.00")
        BigDecimal totalGasto,

        @Schema(description = "Quantidade total de compras realizadas", example = "15")
        Integer totalCompras
) {
}
