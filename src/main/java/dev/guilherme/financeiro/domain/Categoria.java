package dev.guilherme.financeiro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    protected Categoria() {
        // exigido pelo JPA
    }

    public Categoria(String nome, TipoTransacao tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public void atualizar(String nome, TipoTransacao tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }
}
