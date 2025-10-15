package com.bancodigio.purchase.analytics.api.gateway;

import com.bancodigio.purchase.analytics.api.client.VercelClient;
import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import com.bancodigio.purchase.analytics.api.exception.ExternalClientException;
import com.bancodigio.purchase.analytics.api.exception.ExternalNotFoundOrBadRequest;
import com.bancodigio.purchase.analytics.api.exception.ExternalServerException;
import com.bancodigio.purchase.analytics.api.exception.ExternalTimeoutException;
import com.bancodigio.purchase.analytics.api.exception.ExternalUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

/**
 * VercelGateway
 * It handle the possible errors when request the endpoints
 */
@Service
public class VercelGateway {
    private final VercelClient client;

    public VercelGateway(VercelClient client) {
        this.client = client;
    }

    public List<Produto> listarProdutos() {
        try {
            return client.listarProdutos();
        } catch (ExternalClientException e) {
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarProdutos - Falha do cliente externo", e);
        } catch (ExternalServerException e) {
            throw new ExternalUnavailableException("VercelGateway#listarProdutos - Serviço externo instável", e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarProdutos - Blob não encontrado (404).", e);
        } catch (ResourceAccessException e) {
            throw new ExternalTimeoutException("VercelGateway#listarProdutos - Timeout ao chamar serviço externo.", e);
        }
    }

    public List<Cliente> listarClientes() {
        try {
            return client.listarClientes();
        } catch (ExternalClientException e) {
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarClientes - Falha do cliente externo", e);
        } catch (ExternalServerException e) {
            throw new ExternalUnavailableException("VercelGateway#listarClientes - Serviço externo instável", e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalNotFoundOrBadRequest("VercelGateway#listarClientes - Blob não encontrado (404).", e);
        } catch (ResourceAccessException e) {
            throw new ExternalTimeoutException("VercelGateway#listarClientes - Timeout ao chamar serviço externo.", e);
        }
    }
}
