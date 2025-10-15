package com.bancodigio.purchase.analytics.api.controller;

import com.bancodigio.purchase.analytics.api.dto.RecomendacaoClienteResponse;
import com.bancodigio.purchase.analytics.api.service.RecomendacaoService;
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

@WebMvcTest(RecomendacaoController.class)
@DisplayName("RecomendacaoController - Testes de Integração")
class RecomendacaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecomendacaoService recomendacaoService;

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar recomendações para todos os clientes")
    void deveRetornarRecomendacoesParaTodosClientes() throws Exception {
        // Arrange
        List<RecomendacaoClienteResponse> recomendacoes = List.of(
                new RecomendacaoClienteResponse("João Silva", "12345678900", "Tinto",
                        new BigDecimal("150.00"), "2020"),
                new RecomendacaoClienteResponse("Maria Santos", "98765432100", "Branco",
                        new BigDecimal("100.00"), "2021"),
                new RecomendacaoClienteResponse("Pedro Oliveira", "11122233344", "Rosé",
                        new BigDecimal("120.00"), "2022")
        );

        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(recomendacoes);

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome", is("João Silva")))
                .andExpect(jsonPath("$[0].cpf", is("12345678900")))
                .andExpect(jsonPath("$[0].tipoVinho", is("Tinto")))
                .andExpect(jsonPath("$[0].preco", is(150.00)))
                .andExpect(jsonPath("$[0].safra", is("2020")))
                .andExpect(jsonPath("$[1].nome", is("Maria Santos")))
                .andExpect(jsonPath("$[1].cpf", is("98765432100")))
                .andExpect(jsonPath("$[1].tipoVinho", is("Branco")))
                .andExpect(jsonPath("$[1].preco", is(100.00)))
                .andExpect(jsonPath("$[1].safra", is("2021")))
                .andExpect(jsonPath("$[2].nome", is("Pedro Oliveira")))
                .andExpect(jsonPath("$[2].cpf", is("11122233344")))
                .andExpect(jsonPath("$[2].tipoVinho", is("Rosé")))
                .andExpect(jsonPath("$[2].preco", is(120.00)))
                .andExpect(jsonPath("$[2].safra", is("2022")));

        verify(recomendacaoService).recomendarVinhoParaCliente();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar lista vazia quando não houver recomendações")
    void deveRetornarListaVaziaQuandoNaoHouverRecomendacoes() throws Exception {
        // Arrange
        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(recomendacaoService).recomendarVinhoParaCliente();
    }

    @Test
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar 401 quando não autenticado")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar Content-Type application/json")
    void deveRetornarContentTypeJson() throws Exception {
        // Arrange
        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve validar estrutura completa do JSON de resposta")
    void deveValidarEstruturaCompletaDoJsonResposta() throws Exception {
        // Arrange
        List<RecomendacaoClienteResponse> recomendacoes = List.of(
                new RecomendacaoClienteResponse("João Silva", "12345678900", "Tinto",
                        new BigDecimal("150.50"), "2020")
        );

        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(recomendacoes);

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[0].cpf").exists())
                .andExpect(jsonPath("$[0].tipoVinho").exists())
                .andExpect(jsonPath("$[0].preco").exists())
                .andExpect(jsonPath("$[0].safra").exists())
                .andExpect(jsonPath("$[0].nome").isString())
                .andExpect(jsonPath("$[0].cpf").isString())
                .andExpect(jsonPath("$[0].tipoVinho").isString())
                .andExpect(jsonPath("$[0].preco").isNumber())
                .andExpect(jsonPath("$[0].safra").isString());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar recomendações com diferentes tipos de vinho")
    void deveRetornarRecomendacoesComDiferentesTipos() throws Exception {
        // Arrange
        List<RecomendacaoClienteResponse> recomendacoes = List.of(
                new RecomendacaoClienteResponse("Cliente 1", "11111111111", "Tinto",
                        new BigDecimal("200.00"), "2019"),
                new RecomendacaoClienteResponse("Cliente 2", "22222222222", "Branco",
                        new BigDecimal("180.00"), "2020"),
                new RecomendacaoClienteResponse("Cliente 3", "33333333333", "Rosé",
                        new BigDecimal("160.00"), "2021"),
                new RecomendacaoClienteResponse("Cliente 4", "44444444444", "Espumante",
                        new BigDecimal("250.00"), "2022")
        );

        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(recomendacoes);

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].tipoVinho", is("Tinto")))
                .andExpect(jsonPath("$[1].tipoVinho", is("Branco")))
                .andExpect(jsonPath("$[2].tipoVinho", is("Rosé")))
                .andExpect(jsonPath("$[3].tipoVinho", is("Espumante")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar recomendação única por cliente")
    void deveRetornarRecomendacaoUnicaPorCliente() throws Exception {
        // Arrange
        List<RecomendacaoClienteResponse> recomendacoes = List.of(
                new RecomendacaoClienteResponse("João Silva", "12345678900", "Tinto",
                        new BigDecimal("150.00"), "2020")
        );

        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(recomendacoes);

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf", is("12345678900")));

        verify(recomendacaoService).recomendarVinhoParaCliente();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve retornar valores decimais corretos para preço")
    void deveRetornarValoresDecimaisCorretosParaPreco() throws Exception {
        // Arrange
        List<RecomendacaoClienteResponse> recomendacoes = List.of(
                new RecomendacaoClienteResponse("João Silva", "12345678900", "Tinto",
                        new BigDecimal("150.99"), "2020")
        );

        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(recomendacoes);

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].preco", is(150.99)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /v1/recomendacao/cliente/tipo - Deve processar múltiplos clientes com mesmo tipo de vinho")
    void deveProcessarMultiplosClientesComMesmoTipo() throws Exception {
        // Arrange
        List<RecomendacaoClienteResponse> recomendacoes = List.of(
                new RecomendacaoClienteResponse("Cliente 1", "11111111111", "Tinto",
                        new BigDecimal("150.00"), "2020"),
                new RecomendacaoClienteResponse("Cliente 2", "22222222222", "Tinto",
                        new BigDecimal("200.00"), "2019"),
                new RecomendacaoClienteResponse("Cliente 3", "33333333333", "Tinto",
                        new BigDecimal("180.00"), "2021")
        );

        when(recomendacaoService.recomendarVinhoParaCliente()).thenReturn(recomendacoes);

        // Act & Assert
        mockMvc.perform(get("/v1/recomendacao/cliente/tipo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].tipoVinho", is("Tinto")))
                .andExpect(jsonPath("$[1].tipoVinho", is("Tinto")))
                .andExpect(jsonPath("$[2].tipoVinho", is("Tinto")));
    }
}