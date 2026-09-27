package dev.guilherme.financeiro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import java.net.URI;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** A listagem mensal não pode fazer uma consulta extra por categoria (N+1). */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class ListagemTransacoesQueriesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void listarPorMesUsaUmaConsultaMesmoComVariasCategorias() throws Exception {
        for (int i = 1; i <= 3; i++) {
            long categoriaId = criarCategoria("Categoria NMaisUm " + i);
            mockMvc.perform(post("/api/transacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"descricao\":\"t" + i + "\",\"valor\":10.00,\"data\":\"1999-02-0" + i
                                    + "\",\"categoriaId\":" + categoriaId + "}"))
                    .andExpect(status().isCreated());
        }

        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        mockMvc.perform(get("/api/transacoes").param("ano", "1999").param("mes", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));

        // 1 select com join na categoria. Sem o @EntityGraph seriam 1 + 3.
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }

    private long criarCategoria(String nome) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String path = URI.create(res.getResponse().getHeader("Location")).getPath();
        return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    }
}
