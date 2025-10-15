package com.bancodigio.purchase.analytics.api.client;

import com.bancodigio.purchase.analytics.api.dto.Cliente;
import com.bancodigio.purchase.analytics.api.dto.Produto;
import org.springframework.http.MediaType;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * VercelClient
 * Interface to define external vercel api
 */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface VercelClient {

    @GetExchange("/produtos-mnboX5IPl6VgG390FECTKqHsD9SkLS.json")
    List<Produto> listarProdutos();

    @GetExchange("/clientes-Vz1U6aR3GTsjb3W8BRJhcNKmA81pVh.json")
    List<Cliente> listarClientes();
}
