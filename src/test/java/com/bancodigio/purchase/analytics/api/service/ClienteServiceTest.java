package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.ClienteFielResponse;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - Testes Unitários")
class ClienteServiceTest {

    @Mock
    private VercelGateway vercelGateway;

    @Mock
    private CompraMapper compraMapper;

    @InjectMocks
    private ClienteService clienteService;

    private List<Produto> produtos;
    private List<Cliente> clientes;

    @BeforeEach
    void setUp() {
        produtos = List.of(
                new Produto(1001, "Tinto", new BigDecimal("150.00"), "2020", 2023),
                new Produto(1002, "Branco", new BigDecimal("100.00"), "2021", 2023),
                new Produto(1003, "Rosé", new BigDecimal("120.00"), "2022", 2023)
        );

        clientes = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 5),
                        new CompraItem("1002", 2)
                )),
                new Cliente("Maria Santos", "98765432100", List.of(
                        new CompraItem("1001", 10),
                        new CompraItem("1003", 3)
                )),
                new Cliente("Pedro Oliveira", "11122233344", List.of(
                        new CompraItem("1002", 1)
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
    @DisplayName("Deve retornar os 3 clientes mais fiéis ordenados por valor total gasto")
    void deveRetornarClientesFieisOrdenadosPorValorGasto() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).hasSize(3);

        // Maria deve ser a primeira (10 * 150 + 3 * 120 = 1860)
        assertThat(resultado.get(0).cpf()).isEqualTo("98765432100");
        assertThat(resultado.get(0).nome()).isEqualTo("Maria Santos");
        assertThat(resultado.get(0).totalGasto()).isEqualByComparingTo(new BigDecimal("1860.00"));
        assertThat(resultado.get(0).totalCompras()).isEqualTo(2);

        // João deve ser o segundo (5 * 150 + 2 * 100 = 950)
        assertThat(resultado.get(1).cpf()).isEqualTo("12345678900");
        assertThat(resultado.get(1).nome()).isEqualTo("João Silva");
        assertThat(resultado.get(1).totalGasto()).isEqualByComparingTo(new BigDecimal("950.00"));
        assertThat(resultado.get(1).totalCompras()).isEqualTo(2);

        // Pedro deve ser o terceiro (1 * 100 = 100)
        assertThat(resultado.get(2).cpf()).isEqualTo("11122233344");
        assertThat(resultado.get(2).nome()).isEqualTo("Pedro Oliveira");
        assertThat(resultado.get(2).totalGasto()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(resultado.get(2).totalCompras()).isEqualTo(1);

        verify(vercelGateway).listarProdutos();
        verify(vercelGateway).listarClientes();
    }

    @Test
    @DisplayName("Deve retornar apenas top 3 quando houver mais de 3 clientes")
    void deveRetornarApenasTop3Clientes() {
        // Arrange
        List<Cliente> muitosClientes = List.of(
                new Cliente("Cliente 1", "11111111111", List.of(new CompraItem("1001", 20))),
                new Cliente("Cliente 2", "22222222222", List.of(new CompraItem("1001", 15))),
                new Cliente("Cliente 3", "33333333333", List.of(new CompraItem("1001", 10))),
                new Cliente("Cliente 4", "44444444444", List.of(new CompraItem("1001", 5))),
                new Cliente("Cliente 5", "55555555555", List.of(new CompraItem("1001", 1)))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(muitosClientes);

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).cpf()).isEqualTo("11111111111");
        assertThat(resultado.get(1).cpf()).isEqualTo("22222222222");
        assertThat(resultado.get(2).cpf()).isEqualTo("33333333333");
    }

    @Test
    @DisplayName("Deve ignorar compras com código de produto inválido")
    void deveIgnorarComprasComCodigoProdutoInvalido() {
        // Arrange
        List<Cliente> clientesComCodigoInvalido = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("INVALIDO", 5),
                        new CompraItem("1001", 2)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComCodigoInvalido);

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).totalCompras()).isEqualTo(1); // Apenas a compra válida
        assertThat(resultado.get(0).totalGasto()).isEqualByComparingTo(new BigDecimal("300.00")); // 2 * 150
    }

    @Test
    @DisplayName("Deve ignorar compras de produtos não encontrados no catálogo")
    void deveIgnorarComprasDeProdutosNaoEncontrados() {
        // Arrange
        List<Cliente> clientesComProdutoInexistente = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("9999", 5), // Produto não existe
                        new CompraItem("1001", 2)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComProdutoInexistente);

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).totalCompras()).isEqualTo(1); // Apenas a compra válida
        assertThat(resultado.get(0).totalGasto()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("Deve tratar cliente sem compras retornando lista vazia de compras")
    void deveTratarClienteSemCompras() {
        // Arrange
        List<Cliente> clientesSemCompras = List.of(
                new Cliente("João Silva", "12345678900", null),
                new Cliente("Maria Santos", "98765432100", List.of(new CompraItem("1001", 5)))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesSemCompras);

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).cpf()).isEqualTo("98765432100");
    }

    @Test
    @DisplayName("Deve ordenar por quantidade de compras quando valor total for igual")
    void deveOrdenarPorQuantidadeQuandoValorIgual() {
        // Arrange
        List<Cliente> clientesComValorIgual = List.of(
                new Cliente("Cliente A", "11111111111", List.of(
                        new CompraItem("1002", 5) // 5 * 100 = 500, 1 compra
                )),
                new Cliente("Cliente B", "22222222222", List.of(
                        new CompraItem("1002", 2), // 2 * 100 = 200
                        new CompraItem("1002", 3)  // 3 * 100 = 300, total = 500, 2 compras
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesComValorIgual);

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).hasSize(2);
        // Cliente B deve vir primeiro por ter mais compras (mesmo valor total)
        assertThat(resultado.get(0).cpf()).isEqualTo("22222222222");
        assertThat(resultado.get(0).totalCompras()).isEqualTo(2);
        assertThat(resultado.get(1).cpf()).isEqualTo("11111111111");
        assertThat(resultado.get(1).totalCompras()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver clientes")
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(List.of());

        // Act
        List<ClienteFielResponse> resultado = clienteService.clientesFieis();

        // Assert
        assertThat(resultado).isEmpty();
    }
}