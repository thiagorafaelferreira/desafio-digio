package com.bancodigio.purchase.analytics.api.controller;

import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.PageResponse;
import com.bancodigio.purchase.analytics.api.exception.CompraNotFoundException;
import com.bancodigio.purchase.analytics.api.service.CompraService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompraController.class)
@DisplayName("CompraController - Testes de Integração")
class CompraControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompraService compraService;

    @Test
    @WithMockUser
    @DisplayName("GET /v1/compras - Deve retornar lista de compras ordenadas por valor crescente")
    void deveRetornarComprasOrdenadasPorValorCrescente() throws Exception {
        // Arrange
        List<CompraResponse> compras = List.of(
                new CompraResponse("João Silva", "12345678900", 1001, "Tinto", "2020", 2023,
                        new BigDecimal("150.00"), 2, new BigDecimal("300.00")),
                new CompraResponse("Maria Santos", "98765432100", 1002, "Branco", "2021", 2023,
                        new BigDecimal("100.00"), 5, new BigDecimal("500.00")),
                new CompraResponse("Pedro Oliveira", "11122233344", 1003, "Rosé", "2022", 2023,
                        new BigDecimal("120.00"), 10, new BigDecimal("1200.00"))
        );

        PageResponse<CompraResponse> pageResponse = PageResponse.of(compras, 0, 20);
        when(compraService.listarComprasOrdenadasPorValorCrescente(anyInt(), anyInt())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(true)))
                .andExpect(jsonPath("$.content[0].clienteNome", is("João Silva")))
                .andExpect(jsonPath("$.content[0].clienteCpf", is("12345678900")))
                .andExpect(jsonPath("$.content[0].produtoCodigo", is(1001)))
                .andExpect(jsonPath("$.content[0].produtoTipo", is("Tinto")))
                .andExpect(jsonPath("$.content[0].produtoSafra", is("2020")))
                .andExpect(jsonPath("$.content[0].produtoAnoCompra", is(2023)))
                .andExpect(jsonPath("$.content[0].precoUnitario", is(150.00)))
                .andExpect(jsonPath("$.content[0].quantidade", is(2)))
                .andExpect(jsonPath("$.content[0].valorTotal", is(300.00)))
                .andExpect(jsonPath("$.content[1].valorTotal", is(500.00)))
                .andExpect(jsonPath("$.content[2].valorTotal", is(1200.00)));

        verify(compraService).listarComprasOrdenadasPorValorCrescente(0, 20);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/compras - Deve retornar lista vazia quando não houver compras")
    void deveRetornarListaVaziaQuandoNaoHouverCompras() throws Exception {
        // Arrange
        PageResponse<CompraResponse> pageResponse = PageResponse.of(List.of(), 0, 20);
        when(compraService.listarComprasOrdenadasPorValorCrescente(anyInt(), anyInt())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)));

        verify(compraService).listarComprasOrdenadasPorValorCrescente(0, 20);
    }

    @Test
    @DisplayName("GET /v1/compras - Deve retornar 401 quando não autenticado")
    void deveRetornar401QuandoNaoAutenticadoListarCompras() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/maior-compra/{ano} - Deve retornar maior compra do ano especificado")
    void deveRetornarMaiorCompraDoAno() throws Exception {
        // Arrange
        CompraResponse maiorCompra = new CompraResponse(
                "João Silva", "12345678900", 1001, "Tinto", "2020", 2023,
                new BigDecimal("150.00"), 10, new BigDecimal("1500.00")
        );

        when(compraService.maiorCompraDoAno(2023)).thenReturn(maiorCompra);

        // Act & Assert
        mockMvc.perform(get("/v1/maior-compra/2023")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteNome", is("João Silva")))
                .andExpect(jsonPath("$.clienteCpf", is("12345678900")))
                .andExpect(jsonPath("$.produtoCodigo", is(1001)))
                .andExpect(jsonPath("$.produtoTipo", is("Tinto")))
                .andExpect(jsonPath("$.produtoAnoCompra", is(2023)))
                .andExpect(jsonPath("$.valorTotal", is(1500.00)));

        verify(compraService).maiorCompraDoAno(2023);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/maior-compra/{ano} - Deve retornar 404 quando não houver compra no ano")
    void deveRetornar404QuandoNaoHouverCompraNoAno() throws Exception {
        // Arrange
        when(compraService.maiorCompraDoAno(2020))
                .thenThrow(new CompraNotFoundException("Nenhuma compra encontrada para o ano 2020"));

        // Act & Assert
        mockMvc.perform(get("/v1/maior-compra/2020")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Nenhuma compra encontrada para o ano 2020")));

        verify(compraService).maiorCompraDoAno(2020);
    }

    @Test
    @DisplayName("GET /v1/maior-compra/{ano} - Deve retornar 401 quando não autenticado")
    void deveRetornar401QuandoNaoAutenticadoMaiorCompra() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/maior-compra/2023")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/maior-compra/{ano} - Deve validar formato do ano (4 dígitos)")
    void deveValidarFormatoDoAno() throws Exception {
        // Act & Assert - Ano com menos de 4 dígitos
        mockMvc.perform(get("/v1/maior-compra/23")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Ano com mais de 4 dígitos
        mockMvc.perform(get("/v1/maior-compra/20230")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/maior-compra/{ano} - Deve rejeitar ano não numérico")
    void deveRejeitarAnoNaoNumerico() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/maior-compra/abcd")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/compras - Deve validar estrutura completa do JSON de resposta")
    void deveValidarEstruturaCompletaDoJsonResposta() throws Exception {
        // Arrange
        List<CompraResponse> compras = List.of(
                new CompraResponse("João Silva", "12345678900", 1001, "Tinto", "2020", 2023,
                        new BigDecimal("150.00"), 2, new BigDecimal("300.00"))
        );

        PageResponse<CompraResponse> pageResponse = PageResponse.of(compras, 0, 20);
        when(compraService.listarComprasOrdenadasPorValorCrescente(anyInt(), anyInt())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].clienteNome").exists())
                .andExpect(jsonPath("$.content[0].clienteCpf").exists())
                .andExpect(jsonPath("$.content[0].produtoCodigo").exists())
                .andExpect(jsonPath("$.content[0].produtoTipo").exists())
                .andExpect(jsonPath("$.content[0].produtoSafra").exists())
                .andExpect(jsonPath("$.content[0].produtoAnoCompra").exists())
                .andExpect(jsonPath("$.content[0].precoUnitario").exists())
                .andExpect(jsonPath("$.content[0].quantidade").exists())
                .andExpect(jsonPath("$.content[0].valorTotal").exists())
                .andExpect(jsonPath("$.content[0].clienteNome").isString())
                .andExpect(jsonPath("$.content[0].clienteCpf").isString())
                .andExpect(jsonPath("$.content[0].produtoCodigo").isNumber())
                .andExpect(jsonPath("$.content[0].produtoTipo").isString())
                .andExpect(jsonPath("$.content[0].produtoSafra").isString())
                .andExpect(jsonPath("$.content[0].produtoAnoCompra").isNumber())
                .andExpect(jsonPath("$.content[0].precoUnitario").isNumber())
                .andExpect(jsonPath("$.content[0].quantidade").isNumber())
                .andExpect(jsonPath("$.content[0].valorTotal").isNumber());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/maior-compra/{ano} - Deve aceitar anos válidos de 4 dígitos")
    void deveAceitarAnosValidos() throws Exception {
        // Arrange
        CompraResponse compra = new CompraResponse(
                "João Silva", "12345678900", 1001, "Tinto", "2020", 2023,
                new BigDecimal("150.00"), 5, new BigDecimal("750.00")
        );

        when(compraService.maiorCompraDoAno(2023)).thenReturn(compra);
        when(compraService.maiorCompraDoAno(2020)).thenReturn(compra);
        when(compraService.maiorCompraDoAno(1999)).thenReturn(compra);

        // Act & Assert
        mockMvc.perform(get("/v1/maior-compra/2023"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/maior-compra/2020"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/maior-compra/1999"))
                .andExpect(status().isOk());
    }
}