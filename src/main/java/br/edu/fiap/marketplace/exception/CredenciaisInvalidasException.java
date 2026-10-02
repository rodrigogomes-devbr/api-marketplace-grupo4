package br.edu.fiap.marketplace.exception;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
