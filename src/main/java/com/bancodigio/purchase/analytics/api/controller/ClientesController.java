package com.bancodigio.purchase.analytics.api.controller;

import com.bancodigio.purchase.analytics.api.dto.ClienteFielResponse;
import com.bancodigio.purchase.analytics.api.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClientesController
 * Fornece api com informacoes de analise de clientes
 */
@RestController
@RequestMapping("/v1")
@Tag(name = "Clientes", description = "Endpoints para análise de clientes")
public class ClientesController {

    private final ClienteService clienteService;

    public ClientesController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes-fieis")
    @Operation(
            summary = "Lista os clientes mais fiéis",
            description = "Retorna os 3 clientes mais fiéis ordenados por valor total gasto e quantidade de compras"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes fiéis retornada com sucesso"),
            @ApiResponse(responseCode = "503", description = "Serviço externo indisponível"),
            @ApiResponse(responseCode = "504", description = "Timeout ao acessar serviço externo")
    })
    public List<ClienteFielResponse> clientesFieis() {
        return clienteService.clientesFieis();
    }

}
