package dev.guilherme.financeiro.exception;

/** Violação de uma regra de negócio (ex.: nome de categoria duplicado, categoria em uso). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
