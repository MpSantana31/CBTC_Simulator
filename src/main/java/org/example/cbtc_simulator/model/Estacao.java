package org.example.cbtc_simulator.model;

public class Estacao {
    private Integer id;
    private String nome;
    private String zona;

    public Estacao() {}

    public Estacao(String nome, String zona) {
        this.nome = nome;
        this.zona = zona;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}
