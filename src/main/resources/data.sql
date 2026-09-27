-- Seed de exemplo (só roda em H2; em PostgreSQL o Spring pula pelo default
-- spring.sql.init.mode=embedded). Datas no mês corrente para o resumo já mostrar dados.
INSERT INTO categorias (nome, tipo) VALUES
  ('Salário', 'RECEITA'),
  ('Freelance', 'RECEITA'),
  ('Moradia', 'DESPESA'),
  ('Alimentação', 'DESPESA'),
  ('Transporte', 'DESPESA');

INSERT INTO transacoes (descricao, valor, tipo, data, categoria_id, criada_em) VALUES
  ('Salário do mês', 5000.00, 'RECEITA', CURRENT_DATE, (SELECT id FROM categorias WHERE nome = 'Salário'), CURRENT_TIMESTAMP),
  ('Projeto freelance', 1200.00, 'RECEITA', CURRENT_DATE, (SELECT id FROM categorias WHERE nome = 'Freelance'), CURRENT_TIMESTAMP),
  ('Aluguel', 1500.00, 'DESPESA', CURRENT_DATE, (SELECT id FROM categorias WHERE nome = 'Moradia'), CURRENT_TIMESTAMP),
  ('Mercado', 800.00, 'DESPESA', CURRENT_DATE, (SELECT id FROM categorias WHERE nome = 'Alimentação'), CURRENT_TIMESTAMP),
  ('Uber', 200.00, 'DESPESA', CURRENT_DATE, (SELECT id FROM categorias WHERE nome = 'Transporte'), CURRENT_TIMESTAMP);
