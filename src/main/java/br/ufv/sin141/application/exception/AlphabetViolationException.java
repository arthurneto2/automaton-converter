package br.ufv.sin141.application.exception;

public class AlphabetViolationException extends RuntimeException {

    public AlphabetViolationException(String message) {
        super(message);
    }
}
