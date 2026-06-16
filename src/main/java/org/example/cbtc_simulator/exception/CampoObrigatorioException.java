package org.example.cbtc_simulator.exception;

public class CampoObrigatorioException extends RuntimeException {
    public CampoObrigatorioException(String campo) { super("Campo obrigatorio: " + campo); }
}
