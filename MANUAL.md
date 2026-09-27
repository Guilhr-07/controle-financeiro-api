# Manual completo — API de Controle Financeiro

> Antes deste, leia os **[Fundamentos](../MANUAIS/01-FUNDAMENTOS-SPRING-BOOT.md)** e o
> **[Manual do Gestor de Tarefas](../gestor-tarefas-api/MANUAL.md)**. Aqui eu foco no que
> este projeto tem de **novo**: relacionamento entre entidades e **agregação no banco**.

Projeto: **API de finanças pessoais** — categorias, transações (receita/despesa) e um
**resumo mensal** que soma tudo e calcula o saldo. É o marco do **Mês 3** (PostgreSQL,
JPA/Hibernate, Docker, JUnit).

---

## 1. Como rodar

```bash
cd controle-financeiro-api
./mvnw spring-boot:run          # H2 em memória, já com dados de exemplo
```

Veja o resumo do mês atual (já vem semeado):

```bash
curl "http://localhost:8080/api/resumo"
```

Com PostgreSQL de verdade:

```bash
docker compose up -d db
DB_URL=jdbc:postgresql://localhost:5432/financeiro DB_USER=financeiro DB_PASSWORD=financeiro ./mvnw spring-boot:run
```

Testes: `./mvnw test` (5 testes).

---

## 2. O modelo de dados (a novidade nº 1)

Duas tabelas com um **relacionamento**:

```
Categoria (1) ────< (N) Transacao
  id, nome (único), tipo          id, descricao, valor, tipo*, data, categoria_id
```

Uma categoria tem **muitas** transações; cada transação pertence a **uma** categoria.
O `*` é o pulo do gato: **o tipo (RECEITA/DESPESA) da transação vem da categoria** — você
não informa, é derivado. Isso elimina o erro de uma "despesa" apontar para uma categoria
de "receita".

### `domain/Transacao.java` — o relacionamento em código

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "categoria_id", nullable = false)
private Categoria categoria;
```

- `@ManyToOne` → muitas transações para uma categoria.
- `@JoinColumn(name = "categoria_id")` → cria a coluna de chave estrangeira.
- `fetch = LAZY` → só carrega a categoria do banco **quando** você acessá-la (evita
  puxar dados à toa; bom para performance).

O tipo derivado acontece no método privado, chamado pelo construtor e pelo `atualizar`:

```java
private void vincular(Categoria categoria) {
    this.categoria = categoria;
    this.tipo = categoria.getTipo();   // ← tipo herdado da categoria
}
```

E o valor usa `BigDecimal` (nunca `double` para dinheiro — `double` arredonda errado):

```java
@Column(nullable = false, precision = 12, scale = 2)
private BigDecimal valor;              // até 10 dígitos + 2 casas decimais
```

---

## 3. Agregação no banco (a novidade nº 2 — o coração do projeto)

O endpoint `GET /api/resumo?ano=&mes=` devolve totais e saldo. O importante: a **soma é
feita no banco** (rápido, com milhões de linhas), não trazendo tudo para a aplicação.

### `repository/TransacaoRepository.java` — consultas com JPQL

**JPQL** é o "SQL do JPA": você escreve sobre as **entidades**, não sobre as tabelas.

```java
@Query("""
    select coalesce(sum(t.valor), 0)
    from Transacao t
    where t.tipo = :tipo and t.data between :inicio and :fim
    """)
BigDecimal somarPorTipoNoPeriodo(@Param("tipo") TipoTransacao tipo,
                                 @Param("inicio") LocalDate inicio,
                                 @Param("fim") LocalDate fim);
```

- `sum(t.valor)` → o banco soma. `coalesce(…, 0)` → se não houver nada, devolve 0 (em vez
  de `null`).
- `:tipo`, `:inicio`, `:fim` → parâmetros seguros (nada de concatenar string → sem SQL
  injection).
- `between` filtra o período — usei intervalo de datas (portável) em vez de funções tipo
  `MONTH()` que mudam de um banco para outro.

O detalhamento por categoria usa **projeção** (uma interface que o Spring preenche):

```java
@Query("""
    select c.nome as categoria, t.tipo as tipo, sum(t.valor) as total
    from Transacao t join t.categoria c
    where t.data between :inicio and :fim
    group by c.nome, t.tipo
    order by sum(t.valor) desc
    """)
List<TotalPorCategoria> totaisPorCategoriaNoPeriodo(...);
```

`group by` agrupa por categoria+tipo e soma cada grupo. `TotalPorCategoria` é só uma
interface com `getCategoria()/getTipo()/getTotal()` — o Spring cria o objeto sozinho.

### `service/TransacaoService.java` — montando o resumo

```java
public ResumoResponse resumoMensal(int ano, int mes) {
    YearMonth periodo = YearMonth.of(ano, mes);
    LocalDate inicio = periodo.atDay(1);          // primeiro dia do mês
    LocalDate fim    = periodo.atEndOfMonth();     // último dia (cuida de fev, meses de 30/31)

    var receitas = repository.somarPorTipoNoPeriodo(TipoTransacao.RECEITA, inicio, fim);
    var despesas = repository.somarPorTipoNoPeriodo(TipoTransacao.DESPESA, inicio, fim);
    var porCategoria = repository.totaisPorCategoriaNoPeriodo(inicio, fim).stream()
            .map(t -> new ResumoResponse.ItemCategoria(t.getCategoria(), t.getTipo(), t.getTotal()))
            .toList();

    return new ResumoResponse(ano, mes, receitas, despesas,
                              receitas.subtract(despesas), porCategoria);  // saldo = receitas - despesas
}
```

`YearMonth` resolve o "início e fim do mês" sem você calcular dia a dia.

---

## 4. As regras de negócio (onde ficam os `409`)

No `CategoriaService`:

```java
if (repository.existsByNomeIgnoreCase(request.nome()))
    throw new RegraNegocioException("Já existe uma categoria com o nome '" + request.nome() + "'");
```

e ao remover:

```java
if (transacaoRepository.existsByCategoriaId(id))
    throw new RegraNegocioException("Categoria " + id + " tem transações e não pode ser removida");
```

O `GlobalExceptionHandler` transforma `RegraNegocioException` em **409 Conflict** — o
código certo para "seu pedido é válido, mas conflita com o estado atual".

---

## 5. Rotas (resumo)

| Método | Rota | O que faz |
|--------|------|-----------|
| POST | `/api/categorias` | cria (nome único) |
| GET | `/api/categorias` | lista |
| DELETE | `/api/categorias/{id}` | remove (bloqueia se tiver transações) |
| POST | `/api/transacoes` | cria (tipo vem da categoria) |
| GET | `/api/transacoes?ano=&mes=` | lista por mês (padrão: mês atual) |
| PUT/DELETE | `/api/transacoes/{id}` | atualiza / remove |
| GET | `/api/resumo?ano=&mes=` | **totais, saldo e por categoria** |

Requisições prontas em `api.http`.

---

## 6. Detalhe importante dos testes (isolamento)

O `src/test/resources/application.properties` usa um H2 **separado** e desliga a semente
(`spring.sql.init.mode=never`). Por quê? Porque dois contextos de teste compartilhariam o
mesmo banco em memória e a semente rodaria duas vezes → erro de chave duplicada. Os testes
criam a própria massa de dados em um mês isolado (jan/2000) para o resumo bater exato.
*(Essa lição está no Jarbas, "Erros Comuns", `ERR-JAVA-005/006`.)*

---

## 7. Como mexer

- **Nova categoria de tipo novo?** O tipo é enum `TipoTransacao` (RECEITA/DESPESA). Para
  um terceiro tipo (ex.: TRANSFERENCIA), adicione no enum e ajuste o resumo.
- **Resumo por semana em vez de mês?** Troque `YearMonth` por um intervalo de datas no
  `TransacaoService` e crie a rota correspondente.
- **Migrar para PostgreSQL de vez?** Já está pronto: suba o `compose.yaml` e passe as
  variáveis `DB_*`. A semente H2 é pulada automaticamente no Postgres.

Próximo: **[E-commerce (JWT)](../ecommerce-api/MANUAL.md)** (Mês 4).
