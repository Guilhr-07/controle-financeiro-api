package dev.guilherme.financeiro.repository;

import dev.guilherme.financeiro.domain.TipoTransacao;
import java.math.BigDecimal;

/** Projeção para o detalhamento do resumo por categoria. */
public interface TotalPorCategoria {

    String getCategoria();

    TipoTransacao getTipo();

    BigDecimal getTotal();
}
