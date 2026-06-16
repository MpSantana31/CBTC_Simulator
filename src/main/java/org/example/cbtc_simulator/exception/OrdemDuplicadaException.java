package org.example.cbtc_simulator.exception;

public class OrdemDuplicadaException extends RuntimeException {
    public OrdemDuplicadaException(String entidade) {
        super("Ja existe um(a) " + entidade + " com essa ordem no mesmo sentido");
    }
}
