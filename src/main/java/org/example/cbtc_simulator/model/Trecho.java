package org.example.cbtc_simulator.model;

public class Trecho {
    private Integer id;
    private int idLinha;
    private String sentido; // IDA ou VOLTA
    private int idEstacaoOrigem;
    private int idEstacaoDestino;
    private double distanciaKm;
    private int ordem;

    public Trecho() {}

    public Trecho(int idLinha, String sentido, int idEstacaoOrigem, int idEstacaoDestino, double distanciaKm, int ordem) {
        this.idLinha = idLinha;
        this.sentido = sentido;
        this.idEstacaoOrigem = idEstacaoOrigem;
        this.idEstacaoDestino = idEstacaoDestino;
        this.distanciaKm = distanciaKm;
        this.ordem = ordem;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getIdLinha() { return idLinha; }
    public void setIdLinha(int idLinha) { this.idLinha = idLinha; }
    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }
    public int getIdEstacaoOrigem() { return idEstacaoOrigem; }
    public void setIdEstacaoOrigem(int idEstacaoOrigem) { this.idEstacaoOrigem = idEstacaoOrigem; }
    public int getIdEstacaoDestino() { return idEstacaoDestino; }
    public void setIdEstacaoDestino(int idEstacaoDestino) { this.idEstacaoDestino = idEstacaoDestino; }
    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}
