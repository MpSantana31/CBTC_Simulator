package org.example.cbtc_simulator.exception;

public class LinhaSemNomeException extends RuntimeException {
    public LinhaSemNomeException() { super("Nome da linha e obrigatorio"); }
}
