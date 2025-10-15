package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.exception.CompraNotFoundException;
import com.bancodigio.purchase.analytics.api.gateway.VercelGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * CompraService
 * Lida com processamento e analise das compras
 */
@Service
public class CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraService.class);

    private final VercelGateway vercelGateway;

    public CompraService(VercelGateway vercelGateway) {
        this.vercelGateway = vercelGateway;
    }

    public List<CompraResponse> listarComprasOrdenadasPorValorCrescente() {
        log.info("CompraService#listarComprasOrdenadasPorValorCrescente - Iniciado processamento da lista de compras");
        var produtos = vercelGateway.listarProdutos();

        Map<Integer, Produto> produtosMap = produtos.stream()
                .collect(Collectors.toMap(
                        Produto::codigo,
                        Function.identity(),
                        (p1, p2) -> p1,
                        HashMap::new
                ));

        var clientes = vercelGateway.listarClientes();

        var comprasProcessadas = clientes.stream()
                .flatMap(cliente -> Optional.ofNullable(cliente.compras())
                        .orElseGet((List::of))
                        .stream()
                        .map(item -> buildCompraResponse(cliente, item, produtosMap)))
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(CompraResponse::valorTotal))
                        .toList();

        log.info("CompraService#listarComprasOrdenadasPorValorCrescente - Finalizado processamento da lista de compras");

        return comprasProcessadas;
    }

    public CompraResponse maiorCompraDoAno(Integer ano) {
        log.info("CompraService#maiorCompraDoAno - Iniciado processamento de busca da maior compra do ano");
        var produtos = vercelGateway.listarProdutos();

        Map<Integer, Produto> produtosMap = produtos.stream()
                .collect(Collectors.toMap(
                        Produto::codigo,
                        Function.identity(),
                        (p1, p2) -> p1,
                        HashMap::new
                ));

        var clientes = vercelGateway.listarClientes();

        var compraProcessada = clientes.stream()
                .flatMap(cliente -> Optional.ofNullable(cliente.compras())
                        .orElseGet(List::of)
                        .stream()
                        .map(item -> buildCompraResponse(cliente, item, produtosMap)))
                .filter(Objects::nonNull)
                .filter(compraResponse -> Objects.equals(compraResponse.produtoAnoCompra(), ano))
                .sorted(Comparator.comparing(CompraResponse::valorTotal).reversed())
                .max(Comparator.comparing(
                        CompraResponse::valorTotal,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));

        log.info("CompraService#maiorCompraDoAno - Finalizado processamento de busca da maior compra do ano");

        return compraProcessada.orElseThrow(() -> new CompraNotFoundException("Nenhuma compra encontrada para o ano " + ano));
    }

    private static CompraResponse buildCompraResponse(Cliente cliente, CompraItem item, Map<Integer, Produto> produtosMap) {
        log.info("CompraService#buildCompraResponse - Iniciado build compra response");

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

        log.info("CompraService#buildCompraResponse - Finalizado build compra response");

        return compraResponse;
    }
}
