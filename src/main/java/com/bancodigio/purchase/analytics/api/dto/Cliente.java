package com.bancodigio.purchase.analytics.api.dto;

import java.util.List;

public record Cliente(String nome, String cpf, List<CompraItem> compras) {
}
