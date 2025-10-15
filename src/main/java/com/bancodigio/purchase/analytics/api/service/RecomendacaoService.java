package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.dto.RecomendacaoClienteResponse;
import com.bancodigio.purchase.analytics.api.dto.TipoQuantidade;
import com.bancodigio.purchase.analytics.api.gateway.VercelGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * RecomendacaoService
 * Lida com analise de recomendacao
 */
@Service
public class RecomendacaoService {

    private final VercelGateway vercelGateway;

    private static final Logger log = LoggerFactory.getLogger(RecomendacaoService.class);

    public RecomendacaoService(VercelGateway vercelGateway) {
        this.vercelGateway = vercelGateway;
    }

    public List<RecomendacaoClienteResponse> recomendarVinhoParaCliente() {
        log.info("RecomendacaoService#recomendarVinhoParaCliente - Iniciado recomendacao");
        var produtos = vercelGateway.listarProdutos();

        Map<Integer, Produto> produtosMap = produtos.stream()
                .collect(Collectors.toMap(
                        Produto::codigo,
                        Function.identity(),
                        (p1, p2) -> p1,
                        HashMap::new
                ));

        var clientes = vercelGateway.listarClientes();

        Map<String, List<CompraResponse>> comprasAgrupadaPorCpf = clientes.stream()
                .flatMap(cliente -> Optional.ofNullable(cliente.compras())
                        .orElseGet((List::of))
                        .stream()
                        .map(item -> buildCompraResponse(cliente, item, produtosMap)))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(CompraResponse::clienteCpf));

        List<RecomendacaoClienteResponse> recomendacaoParaClientes = comprasAgrupadaPorCpf.entrySet().stream()
                .map(entry -> {
                            String cpf = entry.getKey();
                            List<CompraResponse> compras = entry.getValue();

                            var comprasAgrupadaPorTipo = compras.stream().collect(Collectors.groupingBy(CompraResponse::produtoTipo));

                            var totaisPorTipo = comprasAgrupadaPorTipo.entrySet().stream()
                                    .map(e -> new TipoQuantidade(
                                            e.getKey(),
                                            e.getValue().stream()
                                                    .mapToInt(CompraResponse::quantidade)
                                                    .sum()
                                    ))
                                    .toList();

                            // 3️⃣ Pega o tipo com maior quantidade e se empatar, pega o primeiro
                            var tipoMaisComprado = totaisPorTipo.stream()
                                    .max(Comparator.comparingInt(TipoQuantidade::quantidadeTotal))
                                    .orElseThrow(() -> new NoSuchElementException("Cliente sem compras: " + cpf));

                            // 4️⃣ Encontra a primeira compra desse tipo
                            var primeiraCompraDoTipo = compras.stream()
                                    .filter(c -> c.produtoTipo().equals(tipoMaisComprado.tipo()))
                                    .findFirst()
                                    .orElseThrow();

                            // 5️⃣ Monta a resposta
                            return new RecomendacaoClienteResponse(
                                    primeiraCompraDoTipo.clienteNome(),
                                    cpf,
                                    tipoMaisComprado.tipo(),
                                    primeiraCompraDoTipo.precoUnitario(),
                                    primeiraCompraDoTipo.produtoSafra()
                            );
                        }).toList();

        log.info("RecomendacaoService#recomendarVinhoParaCliente - Finalizado recomendacao");

        return recomendacaoParaClientes;
    }

    private static CompraResponse buildCompraResponse(Cliente cliente, CompraItem item, Map<Integer, Produto> produtosMap) {
        log.info("RecomendacaoService#buildCompraResponse - Iniciado build compra response");

        Integer codigoProduto;
        try {
            codigoProduto = Integer.parseInt(item.codigo());
        } catch (NumberFormatException e) {
            log.warn("Código de produto inválido '{}' para cliente {}", item.codigo(), cliente.cpf());
            return null;
        }
        var produto = produtosMap.get(codigoProduto);
        if (isNull(produto)) {
            log.warn("Produto código {} não encontrado; cliente {}", codigoProduto, cliente.cpf());
            return null;
        }

        var preco = produto.preco();
        var quantidade = item.quantidade();
        var total = preco.multiply(BigDecimal.valueOf(quantidade));

        var compraResponse = new CompraResponse(
                cliente.nome(),
                cliente.cpf(),
                produto.codigo(),
                produto.tipoVinho(),
                produto.safra(),
                produto.anoCompra(),
                preco,
                quantidade,
                total
        );

        log.info("RecomendacaoService#buildCompraResponse - Finalizado build compra response");

        return compraResponse;
    }
}
