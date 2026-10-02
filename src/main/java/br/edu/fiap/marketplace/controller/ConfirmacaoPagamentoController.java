package br.edu.fiap.marketplace.controller;
import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoRequest;
import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoResponse;
import br.edu.fiap.marketplace.service.ConfirmacaoPagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class ConfirmacaoPagamentoController {
    private final ConfirmacaoPagamentoService pagamentoService;
    public ConfirmacaoPagamentoController(
            ConfirmacaoPagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }
    @PostMapping
    @Operation(summary = "Criar confirmação de pagamento pendente")
    @ApiResponse(responseCode = "201", description = "Pagamento criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Autenticação necessária")
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    @ApiResponse(responseCode = "409", description = "Conflito de negócio")
    public ResponseEntity<ConfirmacaoPagamentoResponse> criar(
            @Valid @RequestBody ConfirmacaoPagamentoRequest request) {
        ConfirmacaoPagamentoResponse response =
                pagamentoService.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Consultar pagamento pelo ID")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "401", description = "Autenticação necessária")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public ResponseEntity<ConfirmacaoPagamentoResponse> consultar(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(pagamentoService.consultar(id));
    }
    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar pagamento e finalizar carrinho")
    @ApiResponse(responseCode = "200", description = "Pagamento aprovado")
    @ApiResponse(responseCode = "401", description = "Autenticação necessária")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Conflito de negócio")
    public ResponseEntity<ConfirmacaoPagamentoResponse> aprovar(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(pagamentoService.aprovar(id));
    }
    @PatchMapping("/{id}/recusar")
    @Operation(summary = "Recusar pagamento pendente")
    @ApiResponse(responseCode = "200", description = "Pagamento recusado")
    @ApiResponse(responseCode = "401", description = "Autenticação necessária")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    @ApiResponse(responseCode = "409", description = "Conflito de negócio")
    public ResponseEntity<ConfirmacaoPagamentoResponse> recusar(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(pagamentoService.recusar(id));
    }
}

