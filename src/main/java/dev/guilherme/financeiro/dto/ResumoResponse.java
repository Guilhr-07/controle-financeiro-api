package dev.guilherme.financeiro.dto;

import dev.guilherme.financeiro.domain.TipoTransacao;
import java.math.BigDecimal;
import java.util.List;

/** Resumo financeiro de um mês: totais, saldo e detalhamento por categoria. */
public record ResumoResponse(
        int ano,
        int mes,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        List<ItemCategoria> porCategoria
) {
    public record ItemCategoria(String categoria, TipoTransacao tipo, BigDecimal total) {
    }
}
