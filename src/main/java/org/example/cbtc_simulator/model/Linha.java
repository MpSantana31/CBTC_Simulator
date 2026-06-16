package org.example.cbtc_simulator.model;

public class Linha {
    private Integer id;
    private String nome;
    private String corHex;
    private double velocidadeMaxKmh;
    private String tipoVia; // SUPERFICIE, ELEVADA, SUBTERRANEA

    public Linha() {}

    public Linha(String nome, String corHex, double velocidadeMaxKmh, String tipoVia) {
        this.nome = nome;
        this.corHex = corHex;
        this.velocidadeMaxKmh = velocidadeMaxKmh;
        this.tipoVia = tipoVia;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCorHex() { return corHex; }
    public void setCorHex(String corHex) { this.corHex = corHex; }
    public double getVelocidadeMaxKmh() { return velocidadeMaxKmh; }
    public void setVelocidadeMaxKmh(double velocidadeMaxKmh) { this.velocidadeMaxKmh = velocidadeMaxKmh; }
    public String getTipoVia() { return tipoVia; }
    public void setTipoVia(String tipoVia) { this.tipoVia = tipoVia; }
}
