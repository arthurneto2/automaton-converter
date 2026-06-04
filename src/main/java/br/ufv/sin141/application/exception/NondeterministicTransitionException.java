package br.ufv.sin141.application.exception;

public class NondeterministicTransitionException extends RuntimeException {

    public NondeterministicTransitionException(String message) {
        super(message);
    }
}
