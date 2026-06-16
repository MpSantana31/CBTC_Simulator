package org.example.cbtc_simulator.exception;

public class FKViolationException extends RuntimeException {
    public FKViolationException(String detalhe) {
        super("Operacao bloqueada: " + detalhe);
    }
}
