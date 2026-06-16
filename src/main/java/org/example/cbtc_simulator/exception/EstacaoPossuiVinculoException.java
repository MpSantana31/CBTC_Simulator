package org.example.cbtc_simulator.exception;

public class EstacaoPossuiVinculoException extends RuntimeException {
    public EstacaoPossuiVinculoException(String detalhe) {
        super("Estacao possui vinculo e nao pode ser excluida: " + detalhe);
    }
}
