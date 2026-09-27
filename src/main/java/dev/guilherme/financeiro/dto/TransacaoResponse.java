package dev.guilherme.financeiro.dto;

import dev.guilherme.financeiro.domain.TipoTransacao;
import dev.guilherme.financeiro.domain.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        LocalDate data,
        Long categoriaId,
        String categoriaNome
) {
    public static TransacaoResponse de(Transacao t) {
        return new TransacaoResponse(
                t.getId(),
                t.getDescricao(),
                t.getValor(),
                t.getTipo(),
                t.getData(),
                t.getCategoria().getId(),
                t.getCategoria().getNome()
        );
    }
}
