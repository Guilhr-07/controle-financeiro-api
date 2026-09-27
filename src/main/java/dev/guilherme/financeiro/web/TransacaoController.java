package dev.guilherme.financeiro.web;

import dev.guilherme.financeiro.dto.ResumoResponse;
import dev.guilherme.financeiro.dto.TransacaoRequest;
import dev.guilherme.financeiro.dto.TransacaoResponse;
import dev.guilherme.financeiro.service.TransacaoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.YearMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @PostMapping("/transacoes")
    public ResponseEntity<TransacaoResponse> criar(@Valid @RequestBody TransacaoRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        TransacaoResponse criada = service.criar(request);
        URI location = uriBuilder.path("/api/transacoes/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping("/transacoes")
    public Page<TransacaoResponse> listar(@RequestParam(required = false) Integer ano,
                                          @RequestParam(required = false) Integer mes,
                                          @PageableDefault(size = 20) Pageable pageable) {
        YearMonth periodo = periodoOuAtual(ano, mes);
        return service.listarPorMes(periodo.getYear(), periodo.getMonthValue(), pageable);
    }

    @GetMapping("/transacoes/{id}")
    public TransacaoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/transacoes/{id}")
    public TransacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/transacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo(@RequestParam(required = false) Integer ano,
                                 @RequestParam(required = false) Integer mes) {
        YearMonth periodo = periodoOuAtual(ano, mes);
        return service.resumoMensal(periodo.getYear(), periodo.getMonthValue());
    }

    private YearMonth periodoOuAtual(Integer ano, Integer mes) {
        if (ano != null && mes != null) {
            return YearMonth.of(ano, mes);
        }
        return YearMonth.now();
    }
}
