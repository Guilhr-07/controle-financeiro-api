package dev.guilherme.financeiro.service;

import dev.guilherme.financeiro.domain.Categoria;
import dev.guilherme.financeiro.dto.CategoriaRequest;
import dev.guilherme.financeiro.dto.CategoriaResponse;
import dev.guilherme.financeiro.exception.RecursoNaoEncontradoException;
import dev.guilherme.financeiro.exception.RegraNegocioException;
import dev.guilherme.financeiro.repository.CategoriaRepository;
import dev.guilherme.financeiro.repository.TransacaoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;

    public CategoriaService(CategoriaRepository repository, TransacaoRepository transacaoRepository) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("Já existe uma categoria com o nome '" + request.nome() + "'");
        }
        Categoria categoria = repository.save(new Categoria(request.nome(), request.tipo()));
        return CategoriaResponse.de(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Categoria " + id + " não encontrada");
        }
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Categoria " + id + " tem transações e não pode ser removida");
        }
        repository.deleteById(id);
    }

    /** Uso interno pelos outros serviços. */
    public Categoria buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada"));
    }
}
