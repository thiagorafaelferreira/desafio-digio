package com.bancodigio.purchase.analytics.api.service;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.ClienteFielResponse;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
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
 * ClienteService
 * Lida com analise dos clientes
 */
@Service
public class ClienteService {

    private final VercelGateway vercelGateway;

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    public ClienteService(VercelGateway vercelGateway) {
        this.vercelGateway = vercelGateway;
    }

    public List<ClienteFielResponse> clientesFieis() {
        log.info("ClienteService#clientesFieis - Iniciado analise de clientes fieis");
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

        List<ClienteFielResponse> clientesFieis = comprasAgrupadaPorCpf.entrySet().stream()
                .map(entry -> {
                    String cpf = entry.getKey();
                    List<CompraResponse> compras = entry.getValue();

                    BigDecimal totalGasto = compras.stream()
                            .map(CompraResponse::valorTotal)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Integer totalCompras = compras.size();
                    String nome = compras.getFirst().clienteNome(); // todos têm o mesmo nome

                    return new ClienteFielResponse(cpf, nome, totalGasto, totalCompras);
                })
                .sorted(Comparator
                        .comparing(ClienteFielResponse::totalGasto, Comparator.reverseOrder())
                        .thenComparing(ClienteFielResponse::totalCompras, Comparator.reverseOrder()))
                .limit(3)
                .toList();

        log.info("ClienteService#clientesFieis - Finalizado analise de clientes fieis");

        return clientesFieis;
    }

    private static CompraResponse buildCompraResponse(Cliente cliente, CompraItem item, Map<Integer, Produto> produtosMap) {
        log.info("ClienteService#buildCompraResponse - Iniciado build compra response");

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

        log.info("ClienteService#buildCompraResponse - Finalizado build compra response");

        return compraResponse;
    }
}
