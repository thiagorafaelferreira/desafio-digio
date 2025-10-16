package com.bancodigio.purchase.analytics.api.mapper;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.CompraItem;
import com.bancodigio.purchase.analytics.api.dto.CompraResponse;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * CompraMapper
 * Responsável por mapear dados de Cliente, CompraItem e Produto para CompraResponse
 *
 * Esta classe centraliza a lógica de construção de CompraResponse,
 * eliminando duplicação de código nos Services
 */
@Component
public class CompraMapper {

    private static final Logger log = LoggerFactory.getLogger(CompraMapper.class);

    /**
     * Constrói um CompraResponse a partir de Cliente, CompraItem e mapa de Produtos
     *
     * @param cliente      Cliente que realizou a compra
     * @param item         Item da compra (código e quantidade)
     * @param produtosMap  Mapa de produtos indexados por código
     * @return CompraResponse com todos os dados da compra, ou null se houver erro
     */
    public CompraResponse buildCompraResponse(Cliente cliente, CompraItem item, Map<Integer, Produto> produtosMap) {
        log.debug("CompraMapper#buildCompraResponse - Construindo compra para cliente {} item {}",
            cliente.cpf(), item.codigo());

        // Validar e converter código do produto
        Integer codigoProduto = parseCodigoProduto(item.codigo(), cliente.cpf());
        if (codigoProduto == null) {
            return null;
        }

        // Buscar produto no mapa
        Produto produto = produtosMap.get(codigoProduto);
        if (isNull(produto)) {
            log.warn("CompraMapper#buildCompraResponse - Produto código {} não encontrado; cliente {}",
                codigoProduto, cliente.cpf());
            return null;
        }

        // Calcular valor total
        BigDecimal valorTotal = calcularValorTotal(produto.preco(), item.quantidade());

        // Construir e retornar response
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
    }

    /**
     * Valida e converte o código do produto de String para Integer
     *
     * @param codigoString Código do produto como String
     * @param cpfCliente   CPF do cliente (para logging)
     * @return Código do produto como Integer, ou null se inválido
     */
    private Integer parseCodigoProduto(String codigoString, String cpfCliente) {
        try {
            return Integer.parseInt(codigoString);
        } catch (NumberFormatException e) {
            log.warn("CompraMapper#parseCodigoProduto - Código de produto inválido '{}' para cliente {}",
                codigoString, cpfCliente);
            return null;
        }
    }

    /**
     * Calcula o valor total da compra (preço unitário × quantidade)
     *
     * @param precoUnitario Preço unitário do produto
     * @param quantidade    Quantidade comprada
     * @return Valor total calculado
     */
    private BigDecimal calcularValorTotal(BigDecimal precoUnitario, Integer quantidade) {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}