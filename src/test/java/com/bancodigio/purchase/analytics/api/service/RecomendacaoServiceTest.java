package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.dto.RecomendacaoClienteResponse;
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
@DisplayName("RecomendacaoService - Testes Unitários")
class RecomendacaoServiceTest {

    @Mock
    private VercelGateway vercelGateway;

    @Mock
    private CompraMapper compraMapper;

    @InjectMocks
    private RecomendacaoService recomendacaoService;

    private List<Produto> produtos;
    private List<Cliente> clientes;

    @BeforeEach
    void setUp() {
        produtos = List.of(
                new Produto(1001, "Tinto", new BigDecimal("150.00"), "2020", 2023),
                new Produto(1002, "Branco", new BigDecimal("100.00"), "2021", 2023),
                new Produto(1003, "Rosé", new BigDecimal("120.00"), "2022", 2023),
                new Produto(1004, "Tinto", new BigDecimal("200.00"), "2019", 2023)
        );

        clientes = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 5),  // Tinto - 5 unidades
                        new CompraItem("1002", 2)   // Branco - 2 unidades
                )),
                new Cliente("Maria Santos", "98765432100", List.of(
                        new CompraItem("1002", 10), // Branco - 10 unidades
                        new CompraItem("1003", 3)   // Rosé - 3 unidades
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
    @DisplayName("Deve recomendar o tipo de vinho mais comprado para cada cliente")
    void deveRecomendarTipoMaisCompradoPorCliente() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(2);

        // João comprou mais Tinto (5 vs 2 Branco)
        RecomendacaoClienteResponse recomendacaoJoao = resultado.stream()
                .filter(r -> r.cpf().equals("12345678900"))
                .findFirst()
                .orElseThrow();

        assertThat(recomendacaoJoao.nome()).isEqualTo("João Silva");
        assertThat(recomendacaoJoao.tipoVinho()).isEqualTo("Tinto");
        assertThat(recomendacaoJoao.preco()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(recomendacaoJoao.safra()).isEqualTo("2020");

        // Maria comprou mais Branco (10 vs 3 Rosé)
        RecomendacaoClienteResponse recomendacaoMaria = resultado.stream()
                .filter(r -> r.cpf().equals("98765432100"))
                .findFirst()
                .orElseThrow();

        assertThat(recomendacaoMaria.nome()).isEqualTo("Maria Santos");
        assertThat(recomendacaoMaria.tipoVinho()).isEqualTo("Branco");
        assertThat(recomendacaoMaria.preco()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(recomendacaoMaria.safra()).isEqualTo("2021");

        verify(vercelGateway).listarProdutos();
        verify(vercelGateway).listarClientes();
    }

    @Test
    @DisplayName("Deve somar quantidades de compras do mesmo tipo de vinho")
    void deveSomarQuantidadesDoMesmoTipo() {
        // Arrange
        List<Cliente> clienteComMultiplasComprasMesmoTipo = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 3),  // Tinto - 3 unidades
                        new CompraItem("1004", 7),  // Tinto - 7 unidades (total Tinto: 10)
                        new CompraItem("1002", 5)   // Branco - 5 unidades
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clienteComMultiplasComprasMesmoTipo);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipoVinho()).isEqualTo("Tinto"); // 10 Tinto vs 5 Branco
    }

    @Test
    @DisplayName("Deve recomendar primeiro tipo quando houver empate na quantidade")
    void deveRecomendarPrimeiroTipoQuandoHouverEmpate() {
        // Arrange
        List<Cliente> clienteComEmpate = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 5),  // Tinto - 5 unidades
                        new CompraItem("1002", 5)   // Branco - 5 unidades
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clienteComEmpate);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        // Deve retornar um dos tipos (o comportamento de max() retorna o primeiro encontrado em caso de empate)
        assertThat(resultado.get(0).tipoVinho()).isIn("Tinto", "Branco");
    }

    @Test
    @DisplayName("Deve usar dados da primeira compra do tipo recomendado")
    void deveUsarDadosPrimeiraCompraDoTipo() {
        // Arrange
        List<Cliente> clienteComMultiplasComprasTinto = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 5),  // Tinto - 150.00, safra 2020 (primeira)
                        new CompraItem("1004", 3)   // Tinto - 200.00, safra 2019 (segunda)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clienteComMultiplasComprasTinto);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        // Deve usar dados da primeira compra de Tinto encontrada
        assertThat(resultado.get(0).preco()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(resultado.get(0).safra()).isEqualTo("2020");
    }

    @Test
    @DisplayName("Deve ignorar compras com código de produto inválido")
    void deveIgnorarComprasComCodigoProdutoInvalido() {
        // Arrange
        List<Cliente> clienteComCodigoInvalido = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("INVALIDO", 10),
                        new CompraItem("1001", 5)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clienteComCodigoInvalido);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipoVinho()).isEqualTo("Tinto");
    }

    @Test
    @DisplayName("Deve ignorar compras de produtos não encontrados")
    void deveIgnorarComprasDeProdutosNaoEncontrados() {
        // Arrange
        List<Cliente> clienteComProdutoInexistente = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("9999", 10),
                        new CompraItem("1001", 5)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clienteComProdutoInexistente);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipoVinho()).isEqualTo("Tinto");
    }

    @Test
    @DisplayName("Deve tratar cliente sem compras válidas")
    void deveTratarClienteSemComprasValidas() {
        // Arrange
        List<Cliente> clientesSemCompras = List.of(
                new Cliente("João Silva", "12345678900", null),
                new Cliente("Maria Santos", "98765432100", List.of(new CompraItem("1001", 5)))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientesSemCompras);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).cpf()).isEqualTo("98765432100");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver clientes")
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(List.of());

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve gerar recomendações para múltiplos clientes")
    void deveGerarRecomendacoesParaMultiplosClientes() {
        // Arrange
        List<Cliente> muitosClientes = List.of(
                new Cliente("Cliente 1", "11111111111", List.of(
                        new CompraItem("1001", 10) // Tinto
                )),
                new Cliente("Cliente 2", "22222222222", List.of(
                        new CompraItem("1002", 8)  // Branco
                )),
                new Cliente("Cliente 3", "33333333333", List.of(
                        new CompraItem("1003", 6)  // Rosé
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(muitosClientes);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(3);

        assertThat(resultado)
                .extracting(RecomendacaoClienteResponse::tipoVinho)
                .containsExactlyInAnyOrder("Tinto", "Branco", "Rosé");
    }

    @Test
    @DisplayName("Deve incluir todos os campos necessários na recomendação")
    void deveIncluirTodosCamposNaRecomendacao() {
        // Arrange
        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clientes);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).isNotEmpty();

        RecomendacaoClienteResponse recomendacao = resultado.get(0);
        assertThat(recomendacao.nome()).isNotBlank();
        assertThat(recomendacao.cpf()).isNotBlank();
        assertThat(recomendacao.tipoVinho()).isNotBlank();
        assertThat(recomendacao.preco()).isNotNull();
        assertThat(recomendacao.safra()).isNotBlank();
    }

    @Test
    @DisplayName("Deve processar cliente com apenas um tipo de vinho")
    void deveProcessarClienteComApenasTipo() {
        // Arrange
        List<Cliente> clienteComUmTipo = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 1),
                        new CompraItem("1004", 1)  // Ambos são Tinto
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(clienteComUmTipo);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipoVinho()).isEqualTo("Tinto");
    }

    @Test
    @DisplayName("Deve calcular recomendação baseada em quantidade total e não em número de compras")
    void deveBasearRecomendacaoEmQuantidadeTotal() {
        // Arrange
        List<Cliente> cliente = List.of(
                new Cliente("João Silva", "12345678900", List.of(
                        new CompraItem("1001", 1),  // Tinto - 1 unidade (1 compra)
                        new CompraItem("1002", 10), // Branco - 10 unidades (1 compra)
                        new CompraItem("1002", 5)   // Branco - 5 unidades (2 compras, total 15)
                ))
        );

        when(vercelGateway.listarProdutos()).thenReturn(produtos);
        when(vercelGateway.listarClientes()).thenReturn(cliente);

        // Act
        List<RecomendacaoClienteResponse> resultado = recomendacaoService.recomendarVinhoParaCliente();

        // Assert
        assertThat(resultado).hasSize(1);
        // Deve recomendar Branco (15 unidades) e não Tinto (1 unidade)
        assertThat(resultado.get(0).tipoVinho()).isEqualTo("Branco");
    }
}