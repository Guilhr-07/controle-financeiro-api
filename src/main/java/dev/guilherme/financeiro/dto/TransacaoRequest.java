package dev.guilherme.financeiro.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** O tipo (receita/despesa) não é enviado: é derivado da categoria. */
public record TransacaoRequest(
        @NotBlank(message = "descricao é obrigatória")
        @Size(max = 140, message = "descricao deve ter no máximo 140 caracteres")
        String descricao,

        @NotNull(message = "valor é obrigatório")
        @Positive(message = "valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "valor inválido (máx. 2 casas decimais)")
        BigDecimal valor,

        @NotNull(message = "data é obrigatória")
        @PastOrPresent(message = "data não pode ser futura")
        LocalDate data,

        @NotNull(message = "categoriaId é obrigatório")
        Long categoriaId
) {
}
