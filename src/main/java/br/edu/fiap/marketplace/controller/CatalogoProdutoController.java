package br.edu.fiap.marketplace.controller;

import br.edu.fiap.marketplace.dto.AtualizarQuantidadeRequest;
import br.edu.fiap.marketplace.dto.CatalogoProdutoRequest;
import br.edu.fiap.marketplace.dto.CatalogoProdutoResponse;
import br.edu.fiap.marketplace.service.CatalogoProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Catálogo")
public class CatalogoProdutoController {

    private final CatalogoProdutoService service;

    public CatalogoProdutoController(CatalogoProdutoService service) {
        this.service = service;
    }

    @Operation(summary = "Lista o catálogo de produtos")
    @GetMapping
    public ResponseEntity<List<CatalogoProdutoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(summary = "Busca um produto pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<CatalogoProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Cadastra um novo produto")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<CatalogoProdutoResponse> cadastrar(
            @Valid @RequestBody CatalogoProdutoRequest request) {
        CatalogoProdutoResponse response = service.cadastrar(request);
        URI location = URI.create("/api/produtos/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Atualiza os dados de um produto")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<CatalogoProdutoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CatalogoProdutoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @Operation(summary = "Repõe ou ajusta o estoque de um produto")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/estoque")
    public ResponseEntity<CatalogoProdutoResponse> atualizarEstoque(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarQuantidadeRequest request) {
        return ResponseEntity.ok(service.reporEstoque(id, request.quantidade()));
    }
}