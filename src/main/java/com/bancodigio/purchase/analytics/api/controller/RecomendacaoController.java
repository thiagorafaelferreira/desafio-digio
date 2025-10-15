package com.bancodigio.purchase.analytics.api.controller;

import com.bancodigio.purchase.analytics.api.dto.RecomendacaoClienteResponse;
import com.bancodigio.purchase.analytics.api.service.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RecomendacaoController
 * Fornece api para recomendar produtos para o cliente
 */
@RestController
@RequestMapping("/v1")
@Tag(name = "Recomendações", description = "Endpoints para recomendação de vinhos")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/recomendacao/cliente/tipo")
    @Operation(
            summary = "Recomenda vinhos para clientes",
            description = "Analisa o histórico de compras de cada cliente e recomenda o tipo de vinho que mais compra"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recomendações geradas com sucesso"),
            @ApiResponse(responseCode = "503", description = "Serviço externo indisponível"),
            @ApiResponse(responseCode = "504", description = "Timeout ao acessar serviço externo")
    })
    public List<RecomendacaoClienteResponse> recomendarVinhoParaCliente() {
        return recomendacaoService.recomendarVinhoParaCliente();
    }
}
