package dev.guilherme.financeiro.web;

import dev.guilherme.financeiro.dto.CategoriaRequest;
import dev.guilherme.financeiro.dto.CategoriaResponse;
import dev.guilherme.financeiro.service.CategoriaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        CategoriaResponse criada = service.criar(request);
        URI location = uriBuilder.path("/api/categorias/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
