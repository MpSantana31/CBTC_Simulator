package org.example.cbtc_simulator.model;

public class EventoSimulacao {
    private final int tick;
    private final int idTrem;
    private final TipoEvento tipoEvento;
    private final String mensagem;

    public EventoSimulacao(int tick, int idTrem, TipoEvento tipoEvento, String mensagem) {
        this.tick = tick;
        this.idTrem = idTrem;
        this.tipoEvento = tipoEvento;
        this.mensagem = mensagem;
    }

    public int getTick() { return tick; }
    public int getIdTrem() { return idTrem; }
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public String getMensagem() { return mensagem; }
}
