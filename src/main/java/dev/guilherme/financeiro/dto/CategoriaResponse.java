package dev.guilherme.financeiro.dto;

import dev.guilherme.financeiro.domain.Categoria;
import dev.guilherme.financeiro.domain.TipoTransacao;

public record CategoriaResponse(Long id, String nome, TipoTransacao tipo) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getTipo());
    }
}
