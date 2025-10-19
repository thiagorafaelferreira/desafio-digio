package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.PageResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.exception.CompraNotFoundException;
import com.bancodigio.purchase.analytics.api.gateway.VercelGateway;
import com.bancodigio.purchase.analytics.api.mapper.CompraMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CompraService
 * Lida com processamento e analise das compras
 */
@Service
public class CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraService.class);

    private final VercelGateway vercelGateway;
    private final CompraMapper compraMapper;

    public CompraService(VercelGateway vercelGateway, CompraMapper compraMapper) {
        this.vercelGateway = vercelGateway;
        this.compraMapper = compraMapper;
    }

    /**
     * Lista todas as compras ordenadas por valor total de forma paginada
     *
     * @param page Número da página (começando em 0)
     * @param size Tamanho da página
     * @return Resposta paginada com as compras ordenadas por valor crescente
     */
    public PageResponse<CompraResponse> listarComprasOrdenadasPorValorCrescente(int page, int size) {
        log.info("CompraService#listarComprasOrdenadasPorValorCrescente - Iniciado processamento da lista de compras (page={}, size={})", page, size);
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
                        .map(item -> compraMapper.buildCompraResponse(cliente, item, produtosMap)))
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(CompraResponse::valorTotal))
                        .toList();

        log.info("CompraService#listarComprasOrdenadasPorValorCrescente - Finalizado processamento. Total de compras: {}", comprasProcessadas.size());

        return PageResponse.of(comprasProcessadas, page, size);
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
                        .map(item -> compraMapper.buildCompraResponse(cliente, item, produtosMap)))
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
}
