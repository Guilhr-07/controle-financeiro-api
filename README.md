# Controle Financeiro — API REST (Spring Boot + PostgreSQL)

API de finanças pessoais: categorias, transações (receita/despesa) e **resumo mensal
com agregação**. Projeto marco do **Mês 3** da trilha de backend (PostgreSQL, JPA/Hibernate,
Docker, testes com JUnit). Continuação natural da [API de Gestão de Tarefas](../gestor-tarefas-api).

![tests](https://img.shields.io/badge/tests-passing-brightgreen) ![java](https://img.shields.io/badge/java-21-orange) ![db](https://img.shields.io/badge/db-PostgreSQL-blue)

## Stack

- **Java 21** + **Spring Boot 4.1**
- Spring Data JPA / Hibernate · Bean Validation
- **PostgreSQL** (produção, via Docker) · **H2** (testes)
- `BigDecimal` para valores monetários · agregação via JPQL
- JUnit 5 + MockMvc

## Modelo de dados

```
Categoria (1) ──< (N) Transacao
  nome (único), tipo               descricao, valor, tipo*, data, categoria_id
  * tipo da transação é derivado da categoria (receita/despesa)
```

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/categorias` | cria categoria (nome único) |
| `GET` | `/api/categorias` | lista categorias |
| `DELETE` | `/api/categorias/{id}` | remove (bloqueia se tiver transações) |
| `POST` | `/api/transacoes` | cria transação (tipo vem da categoria) |
| `GET` | `/api/transacoes?ano=&mes=&page=&size=` | lista por mês (padrão: mês atual) |
| `GET` | `/api/transacoes/{id}` | busca |
| `PUT` | `/api/transacoes/{id}` | atualiza |
| `DELETE` | `/api/transacoes/{id}` | remove |
| `GET` | `/api/resumo?ano=&mes=` | **totais, saldo e detalhamento por categoria** |

Erros seguem **ProblemDetail (RFC 7807)**: validação → `400`; não encontrado → `404`;
regra de negócio (nome duplicado, categoria em uso) → `409`.

### Exemplo de resumo

```json
{
  "ano": 2026, "mes": 7,
  "totalReceitas": 6200.00,
  "totalDespesas": 2500.00,
  "saldo": 3700.00,
  "porCategoria": [
    { "categoria": "Salário", "tipo": "RECEITA", "total": 5000.00 },
    { "categoria": "Moradia", "tipo": "DESPESA", "total": 1500.00 }
  ]
}
```

## Como rodar

```bash
# H2 em memória (com dados de exemplo já semeados)
./mvnw spring-boot:run

# PostgreSQL local
docker compose up -d db
DB_URL=jdbc:postgresql://localhost:5432/financeiro DB_USER=financeiro DB_PASSWORD=financeiro ./mvnw spring-boot:run

# tudo no Docker (API + Postgres)
docker compose up
```

Testes: `./mvnw test`. Requisições prontas em [`api.http`](api.http).

## Estrutura

```
src/main/java/dev/guilherme/financeiro
├── domain/        # Categoria, Transacao, TipoTransacao
├── repository/    # Spring Data JPA + queries de agregação (JPQL)
├── dto/           # requests/responses (records) + ResumoResponse
├── service/       # CategoriaService, TransacaoService (resumo mensal)
├── web/           # controllers REST + GlobalExceptionHandler
└── exception/     # RecursoNaoEncontrado, RegraNegocio
```

## Próximo passo (trilha)

- Mês 4: [API de E-commerce](../ecommerce-api) — autenticação JWT, upload de arquivos,
  paginação avançada e Swagger/OpenAPI.
