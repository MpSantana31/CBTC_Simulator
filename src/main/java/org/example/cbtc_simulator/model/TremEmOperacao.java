package org.example.cbtc_simulator.model;

public class TremEmOperacao {
    private int idTrem;
    private int idLinha;
    private int trechoOrdemAtual;
    private double progressoKm;
    private double velocidadeAtualKmh;
    private double velocidadeMax;
    private StatusTrem status;
    private String direcao;
    private int contadorEmbarque;

    public TremEmOperacao(int idTrem, int idLinha, int trechoOrdemAtual, String direcao) {
        this.idTrem = idTrem;
        this.idLinha = idLinha;
        this.trechoOrdemAtual = trechoOrdemAtual;
        this.direcao = direcao;
        this.progressoKm = 0;
        this.velocidadeAtualKmh = 0;
        this.velocidadeMax = 0;
        this.status = StatusTrem.PARADO;
        this.contadorEmbarque = 0;
    }

    public int getIdTrem() { return idTrem; }
    public int getIdLinha() { return idLinha; }
    public int getTrechoOrdemAtual() { return trechoOrdemAtual; }
    public void setTrechoOrdemAtual(int trechoOrdemAtual) { this.trechoOrdemAtual = trechoOrdemAtual; }
    public double getProgressoKm() { return progressoKm; }
    public void setProgressoKm(double progressoKm) { this.progressoKm = progressoKm; }
    public double getVelocidadeAtualKmh() { return velocidadeAtualKmh; }
    public void setVelocidadeAtualKmh(double velocidadeAtualKmh) { this.velocidadeAtualKmh = velocidadeAtualKmh; }
    public double getVelocidadeMax() { return velocidadeMax; }
    public void setVelocidadeMax(double velocidadeMax) { this.velocidadeMax = velocidadeMax; }
    public StatusTrem getStatus() { return status; }
    public void setStatus(StatusTrem status) { this.status = status; }
    public String getDirecao() { return direcao; }
    public int getContadorEmbarque() { return contadorEmbarque; }
    public void setContadorEmbarque(int contadorEmbarque) { this.contadorEmbarque = contadorEmbarque; }
}
