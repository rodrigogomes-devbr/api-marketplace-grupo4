package br.edu.fiap.marketplace.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Tratamento global inicial. O grupo deverá criar exceções de domínio e
 * acrescentar os respectivos métodos para 404, 409 e 401.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(
            MethodArgumentNotValidException erro,
            HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        erro.getBindingResult().getFieldErrors().forEach(fieldError ->
                campos.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return resposta(
                HttpStatus.BAD_REQUEST,
                "Existem campos inválidos na requisição.",
                request,
                campos);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> tratarEntradaInvalida(
            Exception erro,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "A requisição contém um valor ou formato inválido.",
                request,
                Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> tratarInesperado(
            Exception erro,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno não esperado.",
                request,
                Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException erro,
            HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, erro.getMessage(), request, Map.of());
    }

    @ExceptionHandler({ConflitoNegocioException.class, RegraNegocioException.class})
    public ResponseEntity<ApiErrorResponse> tratarConflito(
            RuntimeException erro,
            HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, erro.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiErrorResponse> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException erro,
            HttpServletRequest request) {
        return resposta(HttpStatus.UNAUTHORIZED, erro.getMessage(), request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> resposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos);
        return ResponseEntity.status(status).body(response);
    }
}
