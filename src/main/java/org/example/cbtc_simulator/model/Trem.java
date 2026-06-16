package org.example.cbtc_simulator.model;

public class Trem {
    private Integer id;
    private String modelo;
    private double velocidadeMaxKmh;
    private double aceleracaoMs2;
    private int capacidade;
    private double comprimentoM;

    public Trem() {}

    public Trem(String modelo, double velocidadeMaxKmh, double aceleracaoMs2, int capacidade, double comprimentoM) {
        this.modelo = modelo;
        this.velocidadeMaxKmh = velocidadeMaxKmh;
        this.aceleracaoMs2 = aceleracaoMs2;
        this.capacidade = capacidade;
        this.comprimentoM = comprimentoM;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public double getVelocidadeMaxKmh() { return velocidadeMaxKmh; }
    public void setVelocidadeMaxKmh(double velocidadeMaxKmh) { this.velocidadeMaxKmh = velocidadeMaxKmh; }
    public double getAceleracaoMs2() { return aceleracaoMs2; }
    public void setAceleracaoMs2(double aceleracaoMs2) { this.aceleracaoMs2 = aceleracaoMs2; }
    public int getCapacidade() { return capacidade; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }
    public double getComprimentoM() { return comprimentoM; }
    public void setComprimentoM(double comprimentoM) { this.comprimentoM = comprimentoM; }
}
