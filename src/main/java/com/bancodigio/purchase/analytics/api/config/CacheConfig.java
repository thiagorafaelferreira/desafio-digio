package com.bancodigio.purchase.analytics.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfig
 * Configuração de cache em memória usando Caffeine
 *
 * Cache aplicado em:
 * - produtos: dados de produtos da API Vercel
 * - clientes: dados de clientes da API Vercel
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura o gerenciador de cache com Caffeine
     *
     * Estratégia:
     * - Expiração: 15 minutos após escrita
     * - Tamanho máximo: 100 entradas por cache
     * - Eviction: LRU (Least Recently Used)
     *
     * @return CacheManager configurado
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("produtos", "clientes");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuração do Caffeine Cache
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)  // Cache expira após 15 minutos
                .maximumSize(100)                         // Máximo 100 entradas
                .recordStats();                           // Habilita estatísticas (para métricas)
    }
}