# Controle Financeiro API

[![CI](https://github.com/Guilhr-07/controle-financeiro-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Guilhr-07/controle-financeiro-api/actions/workflows/ci.yml) | Case completo: [guilherme-portfolio.dev/projetos/controle-financeiro-api](https://guilherme-portfolio.dev/projetos/controle-financeiro-api)

API de finanças pessoais: categorias, receitas e despesas, e um resumo do mês com saldo e total por categoria.

**Stack:** Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL, Flyway, Docker, JUnit 5

## Como rodar

Pré-requisito: JDK 21 ou mais novo.

```bash
./mvnw spring-boot:run
```

Sobe com banco em memória e dados de exemplo do mês atual. Com PostgreSQL no Docker:

```bash
docker compose up --build
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

9 testes de integração, rodando no CI a cada push.

## Mais detalhes

Como funciona por dentro, decisões técnicas, limites conhecidos e aprendizados: [MANUAL.md](MANUAL.md).

## Licença

MIT, veja [LICENSE](LICENSE).
