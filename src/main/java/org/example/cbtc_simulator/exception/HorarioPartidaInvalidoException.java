package org.example.cbtc_simulator.exception;

public class HorarioPartidaInvalidoException extends RuntimeException {
    public HorarioPartidaInvalidoException() { super("Horario deve estar no formato HH:MM (ex: 08:30)"); }
}
