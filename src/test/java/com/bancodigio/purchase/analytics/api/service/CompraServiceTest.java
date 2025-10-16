package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.exception.CompraNotFoundException;
import com.bancodigio.purchase.analytics.api.gateway.VercelGateway;
import com.bancodigio.purchase.analytics.api.mapper.CompraMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompraService - Testes Unitários")
class CompraServiceTest {

    @Mock
    private VercelGateway vercelGateway;

    @Mock
    private CompraMapper compraMapper;

    @InjectMocks
    private CompraService compraService;

    private List<Produto> produtos;
    private List<Cliente> clientes;

    @BeforeEach
    void setUp() {
        produtos = List.of(
                new Produto(1001, "Tinto", new BigDecimal("150.00"), "2020", 2023),
                new Produto(1002, "Branco", new BigDecimal("100.00"), "2021", 2023),
                new Produto(1003, "Rosé", new BigDecimal("120.00"), "2022", 2022)
        );

        clientes = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 5), // 750.00
                        new CompraItem("1002", 2)  // 200.00
                )),
                new Cliente("Maria Santos", "98765432100", List.of(
                        new CompraItem("1003", 1)  // 120.00
                ))
        );

        // Mock CompraMapper to return CompraResponse based on input (lenient for tests without clientes)
        lenient().when(compraMapper.buildCompraResponse(any(Cliente.class), any(CompraItem.class), anyMap()))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    CompraItem item = invocation.getArgument(1);
                    var produtosMap = (java.util.Map<Integer, Produto>) invocation.getArgument(2);

                    try {
                        Integer codigoProduto = Integer.parseInt(item.codigo());
                        Produto produto = produtosMap.get(codigoProduto);
                        if (produto == null) {
                            return null;
                        }

                        BigDecimal valorTotal = produto.preco().multiply(BigDecimal.valueOf(item.quantidade()));
                        return new CompraResponse(
                                cliente.nome(),
                                cliente.cpf(),
                                produto.codigo(),
                                produto.tipoVinho(),
                                produto.safra(),
                                produto.anoCompra(),
                                produto.preco(),
                                item.quantidade(),
                                valorTotal
                        );
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });
    }

    @Test
    @DisplayName("Deve listar todas as compras ordenadas por valor crescente")
    void deveListarComprasOrdenadasPorValorCrescente() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        assertThat(resultado).hasSize(3);

        // Verificar ordenação crescente
        assertThat(resultado.get(0).valorTotal()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(resultado.get(0).clienteNome()).isEqualTo("Maria Santos");
        assertThat(resultado.get(0).produtoCodigo()).isEqualTo(1003);

        assertThat(resultado.get(1).valorTotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(resultado.get(1).clienteNome()).isEqualTo("João Silva");
        assertThat(resultado.get(1).produtoCodigo()).isEqualTo(1002);

        assertThat(resultado.get(2).valorTotal()).isEqualByComparingTo(new BigDecimal("750.00"));
        assertThat(resultado.get(2).clienteNome()).isEqualTo("João Silva");
        assertThat(resultado.get(2).produtoCodigo()).isEqualTo(1001);

        verify(vercelGateway).listarProdutos();
        verify(vercelGateway).listarClientes();
    }

    @Test
    @DisplayName("Deve calcular corretamente o valor total das compras")
    void deveCalcularCorretamenteValorTotal() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        CompraResponse compra1 = resultado.stream()
                .filter(c -> c.produtoCodigo().equals(1001))
                .findFirst()
                .orElseThrow();

        assertThat(compra1.precoUnitario()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(compra1.quantidade()).isEqualTo(5);
        assertThat(compra1.valorTotal()).isEqualByComparingTo(new BigDecimal("750.00"));
    }

    @Test
    @DisplayName("Deve retornar todos os dados da compra corretamente")
    void deveRetornarTodosDadosDaCompra() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        CompraResponse compra = resultado.get(0);
        assertThat(compra.clienteNome()).isNotBlank();
        assertThat(compra.clienteCpf()).isNotBlank();
        assertThat(compra.produtoCodigo()).isNotNull();
        assertThat(compra.produtoTipo()).isNotBlank();
        assertThat(compra.produtoSafra()).isNotBlank();
        assertThat(compra.produtoAnoCompra()).isNotNull();
        assertThat(compra.precoUnitario()).isNotNull();
        assertThat(compra.quantidade()).isNotNull();
        assertThat(compra.valorTotal()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar a maior compra do ano especificado")
    void deveRetornarMaiorCompraDoAno() {
        // Arrange
        List<Cliente> clientesComVariosAnos = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 5), // 750.00 - 2023
                        new CompraItem("1002", 2)  // 200.00 - 2023
                )),
                new Cliente("Maria Santos", "98765432100", List.of(
                        new CompraItem("1003", 1)  // 120.00 - 2022
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComVariosAnos);

        // Act
        CompraResponse resultado = compraService.maiorCompraDoAno(2023);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("750.00"));
        assertThat(resultado.produtoAnoCompra()).isEqualTo(2023);
        assertThat(resultado.produtoCodigo()).isEqualTo(1001);
        assertThat(resultado.clienteNome()).isEqualTo("João Silva");

        verify(vercelGateway).listarProdutos();
        verify(vercelGateway).listarClientes();
    }

    @Test
    @DisplayName("Deve lançar exceção quando não houver compra no ano especificado")
    void deveLancarExcecaoQuandoNaoHouverCompraNoAno() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act & Assert
        assertThatThrownBy(() -> compraService.maiorCompraDoAno(2021))
                .isInstanceOf(CompraNotFoundException.class)
                .hasMessageContaining("Nenhuma compra encontrada para o ano 2021");
    }

    @Test
    @DisplayName("Deve retornar maior compra quando houver múltiplas compras no mesmo ano")
    void deveRetornarMaiorCompraEntreMultiplasDoMesmoAno() {
        // Arrange
        List<Cliente> clientesComMuitasCompras = List.of(
                new Cliente("Cliente 1", "11111111111", List.of(
                        new CompraItem("1001", 2)  // 300.00 - 2023
                )),
                new Cliente("Cliente 2", "22222222222", List.of(
                        new CompraItem("1001", 10) // 1500.00 - 2023
                )),
                new Cliente("Cliente 3", "33333333333", List.of(
                        new CompraItem("1002", 5)  // 500.00 - 2023
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComMuitasCompras);

        // Act
        CompraResponse resultado = compraService.maiorCompraDoAno(2023);

        // Assert
        assertThat(resultado.valorTotal()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(resultado.clienteCpf()).isEqualTo("22222222222");
    }

    @Test
    @DisplayName("Deve ignorar compras com código de produto inválido")
    void deveIgnorarComprasComCodigoProdutoInvalido() {
        // Arrange
        List<Cliente> clientesComCodigoInvalido = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("INVALIDO", 10),
                        new CompraItem("1001", 2)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComCodigoInvalido);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).produtoCodigo()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Deve ignorar compras de produtos não encontrados")
    void deveIgnorarComprasDeProdutosNaoEncontrados() {
        // Arrange
        List<Cliente> clientesComProdutoInexistente = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("9999", 10),
                        new CompraItem("1001", 2)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComProdutoInexistente);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).produtoCodigo()).isEqualTo(1001);
    }

    @Test
    @DisplayName("Deve tratar cliente sem compras retornando lista vazia de compras")
    void deveTratarClienteSemCompras() {
        // Arrange
        List<Cliente> clientesSemCompras = List.of(
                new Cliente("João Silva", "12345678900", null),
                new Cliente("Maria Santos", "98765432100", List.of())
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesSemCompras);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver clientes")
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(List.of());

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve preservar informações do cliente e produto na compra")
    void devePreservarInformacoesClienteEProduto() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<CompraResponse> resultado = compraService.listarComprasOrdenadasPorValorCrescente();

        // Assert
        CompraResponse compra = resultado.stream()
                .filter(c -> c.produtoCodigo().equals(1001))
                .findFirst()
                .orElseThrow();

        assertThat(compra.clienteNome()).isEqualTo("João Silva");
        assertThat(compra.clienteCpf()).isEqualTo("12345678900");
        assertThat(compra.produtoTipo()).isEqualTo("Tinto");
        assertThat(compra.produtoSafra()).isEqualTo("2020");
        assertThat(compra.produtoAnoCompra()).isEqualTo(2023);
    }
}