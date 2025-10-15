package com.bancodigio.purchase.analytics.api.controller;

import com.bancodigio.purchase.analytics.api.dto.ClienteFielResponse;
import com.bancodigio.purchase.analytics.api.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientesController.class)
@DisplayName("ClientesController - Testes de Integração")
class ClientesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    @WithMockUser
    @DisplayName("GET /v1/clientes-fieis - Deve retornar lista de clientes fiéis com sucesso")
    void deveRetornarClientesFieisComSucesso() throws Exception {
        // Arrange
        List<ClienteFielResponse> clientesFieis = List.of(
                new ClienteFielResponse("12345678900", "João Silva", new BigDecimal("1500.00"), 10),
                new ClienteFielResponse("98765432100", "Maria Santos", new BigDecimal("1200.00"), 8),
                new ClienteFielResponse("11122233344", "Pedro Oliveira", new BigDecimal("800.00"), 5)
        );

        when(clienteService.clientesFieis()).thenReturn(clientesFieis);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes-fieis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].cpf", is("12345678900")))
                .andExpect(jsonPath("$[0].nome", is("João Silva")))
                .andExpect(jsonPath("$[0].totalGasto", is(1500.00)))
                .andExpect(jsonPath("$[0].totalCompras", is(10)))
                .andExpect(jsonPath("$[1].cpf", is("98765432100")))
                .andExpect(jsonPath("$[1].nome", is("Maria Santos")))
                .andExpect(jsonPath("$[1].totalGasto", is(1200.00)))
                .andExpect(jsonPath("$[1].totalCompras", is(8)))
                .andExpect(jsonPath("$[2].cpf", is("11122233344")))
                .andExpect(jsonPath("$[2].nome", is("Pedro Oliveira")))
                .andExpect(jsonPath("$[2].totalGasto", is(800.00)))
                .andExpect(jsonPath("$[2].totalCompras", is(5)));

        verify(clienteService).clientesFieis();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/clientes-fieis - Deve retornar lista vazia quando não houver clientes")
    void deveRetornarListaVaziaQuandoNaoHouverClientes() throws Exception {
        // Arrange
        when(clienteService.clientesFieis()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/v1/clientes-fieis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(clienteService).clientesFieis();
    }

    @Test
    @DisplayName("GET /v1/clientes-fieis - Deve retornar 401 quando não autenticado")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/clientes-fieis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/clientes-fieis - Deve retornar Content-Type application/json")
    void deveRetornarContentTypeJson() throws Exception {
        // Arrange
        when(clienteService.clientesFieis()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/v1/clientes-fieis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/clientes-fieis - Deve retornar apenas 3 clientes mesmo quando service retornar mais")
    void deveRetornarApenas3ClientesMesmoQuandoHouverMais() throws Exception {
        // Arrange
        List<ClienteFielResponse> clientesFieis = List.of(
                new ClienteFielResponse("11111111111", "Cliente 1", new BigDecimal("1000.00"), 10),
                new ClienteFielResponse("22222222222", "Cliente 2", new BigDecimal("900.00"), 9),
                new ClienteFielResponse("33333333333", "Cliente 3", new BigDecimal("800.00"), 8)
        );

        when(clienteService.clientesFieis()).thenReturn(clientesFieis);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes-fieis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(clienteService).clientesFieis();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/clientes-fieis - Deve validar estrutura completa do JSON de resposta")
    void deveValidarEstruturaCompletaDoJsonResposta() throws Exception {
        // Arrange
        List<ClienteFielResponse> clientesFieis = List.of(
                new ClienteFielResponse("12345678900", "João Silva", new BigDecimal("1500.50"), 15)
        );

        when(clienteService.clientesFieis()).thenReturn(clientesFieis);

        // Act & Assert
        mockMvc.perform(get("/v1/clientes-fieis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpf").exists())
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[0].totalGasto").exists())
                .andExpect(jsonPath("$[0].totalCompras").exists())
                .andExpect(jsonPath("$[0].cpf").isString())
                .andExpect(jsonPath("$[0].nome").isString())
                .andExpect(jsonPath("$[0].totalGasto").isNumber())
                .andExpect(jsonPath("$[0].totalCompras").isNumber());
    }
}