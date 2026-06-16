package org.example.cbtc_simulator.exception;

public class EstacaoSemNomeException extends RuntimeException {
    public EstacaoSemNomeException() { super("Nome da estacao e obrigatorio"); }
}
