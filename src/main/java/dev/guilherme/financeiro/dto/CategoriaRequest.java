package dev.guilherme.financeiro.dto;

import dev.guilherme.financeiro.domain.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 2, max = 60, message = "nome deve ter entre 2 e 60 caracteres")
        String nome,

        @NotNull(message = "tipo é obrigatório (RECEITA ou DESPESA)")
        TipoTransacao tipo
) {
}
