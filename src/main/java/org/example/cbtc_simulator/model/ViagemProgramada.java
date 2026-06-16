package org.example.cbtc_simulator.model;

public class ViagemProgramada {
    private Integer id;
    private int idTrem;
    private int idLinha;
    private String horarioPartida; // HH:MM
    private boolean idaVolta; // true=IDA, false=VOLTA
    private boolean realizada;

    public ViagemProgramada() {}

    public ViagemProgramada(int idTrem, int idLinha, String horarioPartida, boolean idaVolta) {
        this.idTrem = idTrem;
        this.idLinha = idLinha;
        this.horarioPartida = horarioPartida;
        this.idaVolta = idaVolta;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getIdTrem() { return idTrem; }
    public void setIdTrem(int idTrem) { this.idTrem = idTrem; }
    public int getIdLinha() { return idLinha; }
    public void setIdLinha(int idLinha) { this.idLinha = idLinha; }
    public String getHorarioPartida() { return horarioPartida; }
    public void setHorarioPartida(String horarioPartida) { this.horarioPartida = horarioPartida; }
    public boolean isIdaVolta() { return idaVolta; }
    public void setIdaVolta(boolean idaVolta) { this.idaVolta = idaVolta; }
    public boolean isRealizada() { return realizada; }
    public void setRealizada(boolean realizada) { this.realizada = realizada; }
}
