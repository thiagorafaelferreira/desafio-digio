package com.bancodigio.purchase.analytics.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * PageResponse
 * Resposta paginada genérica para endpoints que retornam listas
 *
 * @param <T> Tipo do conteúdo da página
 */
@Schema(description = "Resposta paginada contendo dados e metadados de paginação")
public record PageResponse<T>(
        @Schema(description = "Lista de itens da página atual")
        List<T> content,

        @Schema(description = "Número da página atual (começando em 0)", example = "0")
        int page,

        @Schema(description = "Tamanho da página (quantidade de itens por página)", example = "20")
        int size,

        @Schema(description = "Total de elementos em todas as páginas", example = "150")
        long totalElements,

        @Schema(description = "Total de páginas disponíveis", example = "8")
        int totalPages,

        @Schema(description = "Indica se é a primeira página", example = "true")
        boolean first,

        @Schema(description = "Indica se é a última página", example = "false")
        boolean last
) {
    /**
     * Cria uma resposta paginada a partir de uma lista completa
     *
     * @param allItems Lista completa de itens
     * @param page Número da página (começando em 0)
     * @param size Tamanho da página
     * @param <T> Tipo dos itens
     * @return PageResponse com os itens da página solicitada
     */
    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<T> pageContent = (fromIndex < totalElements)
            ? allItems.subList(fromIndex, toIndex)
            : List.of();

        return new PageResponse<>(
            pageContent,
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }
}