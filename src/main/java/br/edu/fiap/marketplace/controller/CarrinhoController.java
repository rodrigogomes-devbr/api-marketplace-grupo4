package br.edu.fiap.marketplace.controller;

import br.edu.fiap.marketplace.dto.AtualizarQuantidadeRequest;
import br.edu.fiap.marketplace.dto.CarrinhoRequest;
import br.edu.fiap.marketplace.dto.CarrinhoResponse;
import br.edu.fiap.marketplace.service.CarrinhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


/** TODO criar as rotas protegidas de criação, consulta e alteração do carrinho. */
@RestController
@RequestMapping("/api/carrinhos")
@Tag(name = "Carrinhos")
@SecurityRequirement(name = "bearerAuth")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @Operation(summary = "Cria um carrinho para um usuário e produto")
    @PostMapping
    public ResponseEntity<CarrinhoResponse> criar(@Valid @RequestBody CarrinhoRequest request) {
        CarrinhoResponse response = carrinhoService.criar(request);
        URI location = URI.create("/api/carrinhos/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Busca um carrinho pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<CarrinhoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carrinhoService.buscarPorId(id));
    }

    @Operation(summary = "Lista os carrinhos de um usuário")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarrinhoResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carrinhoService.listarPorUsuario(usuarioId));
    }

    @Operation(summary = "Altera a quantidade de um carrinho aberto")
    @PatchMapping("/{id}/quantidade")
    public ResponseEntity<CarrinhoResponse> alterarQuantidade(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarQuantidadeRequest request) {
        return ResponseEntity.ok(carrinhoService.alterarQuantidade(id, request));
    }

    @Operation(summary = "Cancela um carrinho aberto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        carrinhoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }



}
