package dev.guilherme.financeiro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Derivado da categoria — RECEITA ou DESPESA. Persistido para agregação eficiente. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    protected Transacao() {
        // exigido pelo JPA
    }

    public Transacao(String descricao, BigDecimal valor, LocalDate data, Categoria categoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        vincular(categoria);
    }

    @PrePersist
    void aoCriar() {
        this.criadaEm = Instant.now();
    }

    public void atualizar(String descricao, BigDecimal valor, LocalDate data, Categoria categoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        vincular(categoria);
    }

    private void vincular(Categoria categoria) {
        this.categoria = categoria;
        this.tipo = categoria.getTipo();
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
