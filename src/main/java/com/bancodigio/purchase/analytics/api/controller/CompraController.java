package com.bancodigio.purchase.analytics.api.controller;

import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ComprasController
 * Fornece api para recurperar informacoes das compras
 */
@RestController
@RequestMapping("/v1")
@Tag(name = "Compras", description = "Endpoints para consulta de compras realizadas")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping("/compras")
    @Operation(
            summary = "Lista todas as compras",
            description = "Retorna todas as compras ordenadas por valor total em ordem crescente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de compras retornada com sucesso"),
            @ApiResponse(responseCode = "503", description = "Serviço externo indisponível"),
            @ApiResponse(responseCode = "504", description = "Timeout ao acessar serviço externo")
    })
    public List<CompraResponse> listaCompras() {
        return compraService.listarComprasOrdenadasPorValorCrescente();
    }

    @GetMapping("/maior-compra/{ano}")
    @Operation(
            summary = "Busca a maior compra do ano",
            description = "Retorna a compra de maior valor realizada em um ano específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Maior compra do ano retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Ano inválido fornecido"),
            @ApiResponse(responseCode = "404", description = "Nenhuma compra encontrada para o ano especificado"),
            @ApiResponse(responseCode = "503", description = "Serviço externo indisponível"),
            @ApiResponse(responseCode = "504", description = "Timeout ao acessar serviço externo")
    })
    public CompraResponse maiorCompraPor(
            @Parameter(description = "Ano da compra (formato: YYYY)", example = "2023", required = true)
            @PathVariable
            @NotNull
            @Min(value = 1000, message = "deve conter 4 dígitos")
            @Max(value = 9999, message = "deve conter 4 dígitos")
            Integer ano) {
        return compraService.maiorCompraDoAno(ano);
    }
}
