package dev.guilherme.financeiro.service;

import dev.guilherme.financeiro.domain.Categoria;
import dev.guilherme.financeiro.domain.TipoTransacao;
import dev.guilherme.financeiro.domain.Transacao;
import dev.guilherme.financeiro.dto.ResumoResponse;
import dev.guilherme.financeiro.dto.TransacaoRequest;
import dev.guilherme.financeiro.dto.TransacaoResponse;
import dev.guilherme.financeiro.exception.RecursoNaoEncontradoException;
import dev.guilherme.financeiro.repository.TransacaoRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;
    private final CategoriaService categoriaService;

    public TransacaoService(TransacaoRepository repository, CategoriaService categoriaService) {
        this.repository = repository;
        this.categoriaService = categoriaService;
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request) {
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId());
        Transacao transacao = new Transacao(request.descricao(), request.valor(), request.data(), categoria);
        return TransacaoResponse.de(repository.save(transacao));
    }

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> listarPorMes(int ano, int mes, Pageable pageable) {
        YearMonth periodo = YearMonth.of(ano, mes);
        return repository.findByDataBetween(periodo.atDay(1), periodo.atEndOfMonth(), pageable)
                .map(TransacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscarPorId(Long id) {
        return TransacaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request) {
        Transacao transacao = buscarEntidade(id);
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId());
        transacao.atualizar(request.descricao(), request.valor(), request.data(), categoria);
        return TransacaoResponse.de(repository.save(transacao));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Transação " + id + " não encontrada");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumoMensal(int ano, int mes) {
        YearMonth periodo = YearMonth.of(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        var receitas = repository.somarPorTipoNoPeriodo(TipoTransacao.RECEITA, inicio, fim);
        var despesas = repository.somarPorTipoNoPeriodo(TipoTransacao.DESPESA, inicio, fim);

        var porCategoria = repository.totaisPorCategoriaNoPeriodo(inicio, fim).stream()
                .map(t -> new ResumoResponse.ItemCategoria(t.getCategoria(), t.getTipo(), t.getTotal()))
                .toList();

        return new ResumoResponse(ano, mes, receitas, despesas, receitas.subtract(despesas), porCategoria);
    }

    private Transacao buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação " + id + " não encontrada"));
    }
}
