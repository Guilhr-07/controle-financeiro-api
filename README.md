# Controle Financeiro API

[![CI](https://github.com/Guilhr-07/controle-financeiro-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Guilhr-07/controle-financeiro-api/actions/workflows/ci.yml) | Case completo, com as decisões e o que ficou de fora: [guilherme-portfolio.dev/projetos/controle-financeiro-api](https://guilherme-portfolio.dev/projetos/controle-financeiro-api)

API REST de finanças pessoais: categorias, transações de receita e despesa, e um resumo mensal com totais, saldo e soma por categoria calculados no banco. Java 21, Spring Boot 4.1, PostgreSQL.

## Por que existe

Segundo projeto da minha trilha de backend. Depois de um CRUD de uma tabela só (o Gestor de Tarefas), aqui entram relacionamento entre entidades, dinheiro e agregação. São três lugares onde é fácil errar sem perceber: somar em Java o que o banco soma melhor, guardar valor em `double`, e carregar a relação de cada linha numa consulta separada.

## Como funciona

1. Cria categorias (`Salário` como RECEITA, `Moradia` como DESPESA). O nome é único, sem diferenciar maiúsculas.
2. Lança transações apontando para a categoria. O tipo (receita ou despesa) vem da categoria, o cliente não manda.
3. Consulta o mês: `GET /api/resumo?ano=2026&mes=9`.

```json
{
  "ano": 2026, "mes": 9,
  "totalReceitas": 5000.00,
  "totalDespesas": 1500.00,
  "saldo": 3500.00,
  "porCategoria": [
    { "categoria": "Salário", "tipo": "RECEITA", "total": 5000.00 },
    { "categoria": "Moradia", "tipo": "DESPESA", "total": 1500.00 }
  ]
}
```

(Resposta real de uma execução contra o PostgreSQL do `compose.yaml`.)

4. Categoria com transação não pode ser apagada: 409.

## Arquitetura

```
cliente HTTP -> web (controllers, handler de erro) -> service (@Transactional) -> repository (Spring Data JPA + JPQL) -> H2 ou PostgreSQL
```

```
Categoria (1) ---< (N) Transacao
  nome (único), tipo      descricao, valor, tipo, data, categoria_id
```

## Decisões técnicas

| Decisão | Por quê |
| --- | --- |
| `BigDecimal` com `numeric(12,2)` | Dinheiro em ponto flutuante erra centavo |
| Tipo da transação copiado da categoria e gravado | O resumo filtra por `tipo` sem join; a regra de derivação fica em `Transacao.vincular()` |
| Soma e agrupamento em JPQL (`sum`, `group by`) | O banco devolve 2 números e uma linha por categoria, em vez de a API carregar o mês inteiro para somar |
| `@EntityGraph(attributePaths = "categoria")` na listagem | A resposta mostra o nome da categoria; sem isso uma página com 3 categorias fazia 4 selects (N+1) |
| `CHECK (valor > 0)`, FK e unicidade no banco, além do Bean Validation | A aplicação valida; o banco é a última linha se algum caminho escapar |
| Flyway só no perfil `postgres`, com `ddl-auto=validate` | Banco de verdade com histórico de schema; H2 de dev continua simples |

## Rodando localmente

Pré-requisito: JDK 21 ou mais novo.

```bash
./mvnw spring-boot:run          # H2 em memória, com categorias e transações de exemplo do mês atual
```

Com PostgreSQL:

```bash
docker compose up --build       # API + Postgres, perfil postgres, schema pelo Flyway
```

Ou só o banco no Docker e a API local:

```bash
docker compose up -d db
SPRING_PROFILES_ACTIVE=postgres DB_URL=jdbc:postgresql://localhost:5432/financeiro \
DB_USER=financeiro DB_PASSWORD=financeiro ./mvnw spring-boot:run
```

Requisições prontas em [`api.http`](api.http).

## Endpoints

9 rotas.

| Método | Rota | Resposta |
| --- | --- | --- |
| `POST` | `/api/categorias` | 201, 400, 409 (nome repetido) |
| `GET` | `/api/categorias` | 200 (lista) |
| `DELETE` | `/api/categorias/{id}` | 204, 404, 409 (tem transações) |
| `POST` | `/api/transacoes` | 201, 400, 404 (categoria) |
| `GET` | `/api/transacoes?ano=&mes=&page=&size=&sort=` | 200 (página; sem ano e mês usa o mês atual), 400 |
| `GET` | `/api/transacoes/{id}` | 200, 404 |
| `PUT` | `/api/transacoes/{id}` | 200, 400, 404 |
| `DELETE` | `/api/transacoes/{id}` | 204, 404 |
| `GET` | `/api/resumo?ano=&mes=` | 200, 400 (mês fora de 1 a 12) |

Erros em `ProblemDetail`. Transação exige valor positivo com até 2 casas e data que não esteja no futuro.

## Testes

```bash
./mvnw verify
```

9 testes de integração (`@SpringBootTest` + MockMvc, H2):

- `FinanceiroApiIntegrationTest` (6): fluxo completo com o resumo agregado, nome duplicado, categoria inexistente, valor negativo, mês 13 e `sort` inválido.
- `ListagemTransacoesQueriesTest` (1): conta os `PreparedStatement` do Hibernate na listagem mensal e exige 1. Falhava com 4 antes do `@EntityGraph`.
- `MigracaoFlywayTest` (1): aplica as migrations num H2 em modo PostgreSQL com `ddl-auto=validate`.
- `ControleFinanceiroApiApplicationTests` (1): o contexto sobe.

O CI roda `./mvnw -B verify` em todo push e PR.

## O que ficou de fora

- Não há usuário: todos os dados são de todo mundo. Multi-usuário exigiria `usuario_id` em todas as consultas.
- `GET /api/categorias` devolve a lista inteira, sem paginação. Para finanças pessoais são dezenas; ainda assim é um endpoint sem limite.
- Não existe rota para editar categoria, embora a entidade tenha `atualizar()`.
- A checagem de nome duplicado ignora maiúsculas, mas a constraint do banco não. Duas requisições simultâneas com "Lazer" e "lazer" passariam; um índice único em `lower(nome)` resolve.
- Informar só `ano` sem `mes` cai silenciosamente no mês atual.
- O teste de migração usa H2 em modo PostgreSQL, não um Postgres real (Testcontainers seria o passo seguinte).

## Aprendizados

- Relação `LAZY` não evita N+1 quando o DTO de saída lê a relação: só adia. Contar consultas no teste (`Statistics.getPrepareStatementCount()`) transforma isso em algo que o CI pega.
- `YearMonth.of(2026, 13)` lança `DateTimeException`, e sem tratamento isso era 500 para um erro de digitação do cliente.

## Licença

MIT, veja [LICENSE](LICENSE).
