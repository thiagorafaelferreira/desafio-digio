package com.bancodigio.purchase.analytics.api.gateway;

import com.bancodigio.purchase.analytics.api.client.VercelClient;
import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.exception.ExternalClientException;
import com.bancodigio.purchase.analytics.api.exception.ExternalNotFoundOrBadRequest;
import com.bancodigio.purchase.analytics.api.exception.ExternalServerException;
import com.bancodigio.purchase.analytics.api.exception.ExternalTimeoutException;
import com.bancodigio.purchase.analytics.api.exception.ExternalUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.List;

/**
 * VercelGateway
 * It handle the possible errors when request the endpoints
 * Cache aplicado para reduzir chamadas à API externa
 */
@Service
public class VercelGateway {

    private static final Logger log = LoggerFactory.getLogger(VercelGateway.class);
    private final VercelClient client;

    public VercelGateway(VercelClient client) {
        this.client = client;
    }

    /**
     * Lista produtos da API Vercel com cache de 15 minutos e circuit breaker
     *
     * Cache Key: "produtos" (único para todos)
     * TTL: 15 minutos (configurado em CacheConfig)
     * Circuit Breaker: vercelApi (configurado em application.yml)
     *
     * @return Lista de produtos
     */
    @Cacheable(value = "produtos")
    @CircuitBreaker(name = "vercelApi", fallbackMethod = "listarProdutosFallback")
    public List<Produto> listarProdutos() {
        log.info("VercelGateway#listarProdutos - Buscando produtos da API externa (CACHE MISS)");
        try {
            List<Produto> produtos = client.listarProdutos();
            log.info("VercelGateway#listarProdutos - {} produtos retornados", produtos.size());
            return produtos;
        } catch (ExternalClientException e) {
            log.error("VercelGateway#listarProdutos - Erro no cliente externo", e);
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarProdutos - Falha do cliente externo", e);
        } catch (ExternalServerException e) {
            log.error("VercelGateway#listarProdutos - Erro no servidor externo", e);
            throw new ExternalUnavailableException("VercelGateway#listarProdutos - Serviço externo instável", e);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("VercelGateway#listarProdutos - Blob não encontrado (404)", e);
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarProdutos - Blob não encontrado (404).", e);
        } catch (ResourceAccessException e) {
            log.error("VercelGateway#listarProdutos - Timeout", e);
            throw new ExternalTimeoutException("VercelGateway#listarProdutos - Timeout ao chamar serviço externo.", e);
        }
    }

    /**
     * Lista clientes da API Vercel com cache de 15 minutos e circuit breaker
     *
     * Cache Key: "clientes" (único para todos)
     * TTL: 15 minutos (configurado em CacheConfig)
     * Circuit Breaker: vercelApi (configurado em application.yml)
     *
     * @return Lista de clientes
     */
    @Cacheable(value = "clientes")
    @CircuitBreaker(name = "vercelApi", fallbackMethod = "listarClientesFallback")
    public List<Cliente> listarClientes() {
        log.info("VercelGateway#listarClientes - Buscando clientes da API externa (CACHE MISS)");
        try {
            List<Cliente> clientes = client.listarClientes();
            log.info("VercelGateway#listarClientes - {} clientes retornados", clientes.size());
            return clientes;
        } catch (ExternalClientException e) {
            log.error("VercelGateway#listarClientes - Erro no cliente externo", e);
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarClientes - Falha do cliente externo", e);
        } catch (ExternalServerException e) {
            log.error("VercelGateway#listarClientes - Erro no servidor externo", e);
            throw new ExternalUnavailableException("VercelGateway#listarClientes - Serviço externo instável", e);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("VercelGateway#listarClientes - Blob não encontrado (404)", e);
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarClientes - Blob não encontrado (404).", e);
        } catch (ResourceAccessException e) {
            log.error("VercelGateway#listarClientes - Timeout", e);
            throw new ExternalTimeoutException("VercelGateway#listarClientes - Timeout ao chamar serviço externo.", e);
        }
    }

    /**
     * Método fallback para listarProdutos quando o circuit breaker está aberto
     * Retorna uma lista vazia para evitar propagação de erros
     *
     * @param throwable A exceção que causou a ativação do fallback
     * @return Lista vazia de produtos
     */
    private List<Produto> listarProdutosFallback(Throwable throwable) {
        log.warn("VercelGateway#listarProdutosFallback - Circuit breaker ABERTO ou erro na chamada. Retornando lista vazia. Causa: {}",
                throwable.getMessage());
        return Collections.emptyList();
    }

    /**
     * Método fallback para listarClientes quando o circuit breaker está aberto
     * Retorna uma lista vazia para evitar propagação de erros
     *
     * @param throwable A exceção que causou a ativação do fallback
     * @return Lista vazia de clientes
     */
    private List<Cliente> listarClientesFallback(Throwable throwable) {
        log.warn("VercelGateway#listarClientesFallback - Circuit breaker ABERTO ou erro na chamada. Retornando lista vazia. Causa: {}",
                throwable.getMessage());
        return Collections.emptyList();
    }
}
