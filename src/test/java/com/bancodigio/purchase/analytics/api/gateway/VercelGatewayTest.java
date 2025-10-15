package com.bancodigio.purchase.analytics.api.gateway;

import com.bancodigio.purchase.analytics.api.client.VercelClient;
import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.exception.ExternalClientException;
import com.bancodigio.purchase.analytics.api.exception.ExternalNotFoundOrBadRequest;
import com.bancodigio.purchase.analytics.api.exception.ExternalServerException;
import com.bancodigio.purchase.analytics.api.exception.ExternalTimeoutException;
import com.bancodigio.purchase.analytics.api.exception.ExternalUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VercelGateway - Testes Unitários")
class VercelGatewayTest {

    @Mock
    private VercelClient vercelClient;

    @InjectMocks
    private VercelGateway vercelGateway;

    private List<Produto> produtosEsperados;
    private List<Cliente> clientesEsperados;

    @BeforeEach
    void setUp() {
        produtosEsperados = List.of(
                new Produto(1001, "Tinto", new BigDecimal("150.00"), "2020", 2023),
                new Produto(1002, "Branco", new BigDecimal("100.00"), "2021", 2023)
        );

        clientesEsperados = List.of(
                new Cliente("João Silva", "12345678900", List.of()),
                new Cliente("Maria Santos", "98765432100", List.of())
        );
    }

    @Test
    @DisplayName("Deve listar produtos com sucesso")
    void deveListarProdutosComSucesso() {
        // Arrange
        when(vercelClient.listarProdutos()).thenReturn(produtosEsperados);

        // Act
        List<Produto> resultado = vercelGateway.listarProdutos();

        // Assert
        assertThat(resultado).isEqualTo(produtosEsperados);
        assertThat(resultado).hasSize(2);
        verify(vercelClient).listarProdutos();
    }

    @Test
    @DisplayName("Deve listar clientes com sucesso")
    void deveListarClientesComSucesso() {
        // Arrange
        when(vercelClient.listarClientes()).thenReturn(clientesEsperados);

        // Act
        List<Cliente> resultado = vercelGateway.listarClientes();

        // Assert
        assertThat(resultado).isEqualTo(clientesEsperados);
        assertThat(resultado).hasSize(2);
        verify(vercelClient).listarClientes();
    }

    @Test
    @DisplayName("Deve lançar ExternalNotFoundOrBadRequest quando listarProdutos lançar ExternalClientException")
    void deveLancarExternalNotFoundQuandoListarProdutosLancarExternalClientException() {
        // Arrange
        when(vercelClient.listarProdutos()).thenThrow(new ExternalClientException("Erro do cliente", new Throwable()));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarProdutos())
                .isInstanceOf(ExternalNotFoundOrBadRequest.class)
                .hasMessageContaining("VercelGateway#listarProdutos - Falha do cliente externo")
                .hasCauseInstanceOf(ExternalClientException.class);

        verify(vercelClient).listarProdutos();
    }

    @Test
    @DisplayName("Deve lançar ExternalNotFoundOrBadRequest quando listarClientes lançar ExternalClientException")
    void deveLancarExternalNotFoundQuandoListarClientesLancarExternalClientException() {
        // Arrange
        when(vercelClient.listarClientes()).thenThrow(new ExternalClientException("Erro do cliente", new Throwable()));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarClientes())
                .isInstanceOf(ExternalNotFoundOrBadRequest.class)
                .hasMessageContaining("VercelGateway#listarClientes - Falha do cliente externo")
                .hasCauseInstanceOf(ExternalClientException.class);

        verify(vercelClient).listarClientes();
    }

    @Test
    @DisplayName("Deve lançar ExternalUnavailableException quando listarProdutos lançar ExternalServerException")
    void deveLancarExternalUnavailableQuandoListarProdutosLancarExternalServerException() {
        // Arrange
        when(vercelClient.listarProdutos()).thenThrow(new ExternalServerException("Erro do servidor", new Throwable()));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarProdutos())
                .isInstanceOf(ExternalUnavailableException.class)
                .hasMessageContaining("VercelGateway#listarProdutos - Serviço externo instável")
                .hasCauseInstanceOf(ExternalServerException.class);

        verify(vercelClient).listarProdutos();
    }

    @Test
    @DisplayName("Deve lançar ExternalUnavailableException quando listarClientes lançar ExternalServerException")
    void deveLancarExternalUnavailableQuandoListarClientesLancarExternalServerException() {
        // Arrange
        when(vercelClient.listarClientes()).thenThrow(new ExternalServerException("Erro do servidor", new Throwable()));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarClientes())
                .isInstanceOf(ExternalUnavailableException.class)
                .hasMessageContaining("VercelGateway#listarClientes - Serviço externo instável")
                .hasCauseInstanceOf(ExternalServerException.class);

        verify(vercelClient).listarClientes();
    }

    @Test
    @DisplayName("Deve lançar ExternalNotFoundOrBadRequest quando listarProdutos retornar 404")
    void deveLancarExternalNotFoundQuandoListarProdutosRetornar404() {
        // Arrange
        when(vercelClient.listarProdutos()).thenThrow(HttpClientErrorException.NotFound.create(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                null,
                null
        ));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarProdutos())
                .isInstanceOf(ExternalNotFoundOrBadRequest.class)
                .hasMessageContaining("VercelGateway#listarProdutos - Blob não encontrado (404)")
                .hasCauseInstanceOf(HttpClientErrorException.NotFound.class);

        verify(vercelClient).listarProdutos();
    }

    @Test
    @DisplayName("Deve lançar ExternalNotFoundOrBadRequest quando listarClientes retornar 404")
    void deveLancarExternalNotFoundQuandoListarClientesRetornar404() {
        // Arrange
        when(vercelClient.listarClientes()).thenThrow(HttpClientErrorException.NotFound.create(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                null,
                null
        ));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarClientes())
                .isInstanceOf(ExternalNotFoundOrBadRequest.class)
                .hasMessageContaining("VercelGateway#listarClientes - Blob não encontrado (404)")
                .hasCauseInstanceOf(HttpClientErrorException.NotFound.class);

        verify(vercelClient).listarClientes();
    }

    @Test
    @DisplayName("Deve lançar ExternalTimeoutException quando listarProdutos lançar ResourceAccessException")
    void deveLancarExternalTimeoutQuandoListarProdutosLancarResourceAccessException() {
        // Arrange
        when(vercelClient.listarProdutos()).thenThrow(new ResourceAccessException("Timeout"));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarProdutos())
                .isInstanceOf(ExternalTimeoutException.class)
                .hasMessageContaining("VercelGateway#listarProdutos - Timeout ao chamar serviço externo")
                .hasCauseInstanceOf(ResourceAccessException.class);

        verify(vercelClient).listarProdutos();
    }

    @Test
    @DisplayName("Deve lançar ExternalTimeoutException quando listarClientes lançar ResourceAccessException")
    void deveLancarExternalTimeoutQuandoListarClientesLancarResourceAccessException() {
        // Arrange
        when(vercelClient.listarClientes()).thenThrow(new ResourceAccessException("Timeout"));

        // Act & Assert
        assertThatThrownBy(() -> vercelGateway.listarClientes())
                .isInstanceOf(ExternalTimeoutException.class)
                .hasMessageContaining("VercelGateway#listarClientes - Timeout ao chamar serviço externo")
                .hasCauseInstanceOf(ResourceAccessException.class);

        verify(vercelClient).listarClientes();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando client retornar lista vazia de produtos")
    void deveRetornarListaVaziaQuandoClientRetornarListaVaziaDeProdutos() {
        // Arrange
        when(vercelClient.listarProdutos()).thenReturn(List.of());

        // Act
        List<Produto> resultado = vercelGateway.listarProdutos();

        // Assert
        assertThat(resultado).isEmpty();
        verify(vercelClient).listarProdutos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando client retornar lista vazia de clientes")
    void deveRetornarListaVaziaQuandoClientRetornarListaVaziaDeClientes() {
        // Arrange
        when(vercelClient.listarClientes()).thenReturn(List.of());

        // Act
        List<Cliente> resultado = vercelGateway.listarClientes();

        // Assert
        assertThat(resultado).isEmpty();
        verify(vercelClient).listarClientes();
    }
}