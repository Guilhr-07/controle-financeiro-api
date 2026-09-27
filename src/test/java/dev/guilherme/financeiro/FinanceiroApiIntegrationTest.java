package dev.guilherme.financeiro;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class FinanceiroApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private long criarCategoria(String nome, String tipo) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"tipo\":\"" + tipo + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return idDoLocation(res);
    }

    private static long idDoLocation(MvcResult res) {
        URI location = URI.create(res.getResponse().getHeader("Location"));
        String[] partes = location.getPath().split("/");
        return Long.parseLong(partes[partes.length - 1]);
    }

    @Test
    void fluxoCompletoComResumoAgregado() throws Exception {
        long receitaCat = criarCategoria("Salario Teste", "RECEITA");
        long despesaCat = criarCategoria("Aluguel Teste", "DESPESA");

        // transação de receita — tipo derivado da categoria
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Salario\",\"valor\":1000.00,\"data\":\"2000-01-15\",\"categoriaId\":" + receitaCat + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo", is("RECEITA")));

        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Aluguel\",\"valor\":400.00,\"data\":\"2000-01-20\",\"categoriaId\":" + despesaCat + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo", is("DESPESA")));

        // resumo do mês isolado (jan/2000) não sofre do seed (mês corrente)
        mockMvc.perform(get("/api/resumo").param("ano", "2000").param("mes", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(1000.0))
                .andExpect(jsonPath("$.totalDespesas").value(400.0))
                .andExpect(jsonPath("$.saldo").value(600.0))
                .andExpect(jsonPath("$.porCategoria.length()", is(2)));
    }

    @Test
    void rejeitaCategoriaComNomeDuplicado() throws Exception {
        criarCategoria("Categoria Unica", "DESPESA");
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Categoria Unica\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Regra de negócio violada")));
    }

    @Test
    void rejeitaTransacaoComCategoriaInexistente() throws Exception {
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"x\",\"valor\":10.00,\"data\":\"2000-01-10\",\"categoriaId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Recurso não encontrado")));
    }

    @Test
    void rejeitaValorNegativo() throws Exception {
        long cat = criarCategoria("Cat Validacao", "DESPESA");
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"x\",\"valor\":-5.00,\"data\":\"2000-01-10\",\"categoriaId\":" + cat + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.valor").exists());
    }
}
