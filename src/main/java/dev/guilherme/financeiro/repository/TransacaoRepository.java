package dev.guilherme.financeiro.repository;

import dev.guilherme.financeiro.domain.TipoTransacao;
import dev.guilherme.financeiro.domain.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Page<Transacao> findByDataBetween(LocalDate inicio, LocalDate fim, Pageable pageable);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("""
            select coalesce(sum(t.valor), 0)
            from Transacao t
            where t.tipo = :tipo and t.data between :inicio and :fim
            """)
    BigDecimal somarPorTipoNoPeriodo(@Param("tipo") TipoTransacao tipo,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    @Query("""
            select c.nome as categoria, t.tipo as tipo, sum(t.valor) as total
            from Transacao t join t.categoria c
            where t.data between :inicio and :fim
            group by c.nome, t.tipo
            order by sum(t.valor) desc
            """)
    List<TotalPorCategoria> totaisPorCategoriaNoPeriodo(@Param("inicio") LocalDate inicio,
                                                        @Param("fim") LocalDate fim);
}
