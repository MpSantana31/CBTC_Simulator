package org.example.cbtc_simulator.model;

public class LinhaEstacao {
    private Integer id;
    private int idLinha;
    private int idEstacao;
    private int ordem;
    private String sentido; // IDA ou VOLTA

    public LinhaEstacao() {}

    public LinhaEstacao(int idLinha, int idEstacao, int ordem, String sentido) {
        this.idLinha = idLinha;
        this.idEstacao = idEstacao;
        this.ordem = ordem;
        this.sentido = sentido;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getIdLinha() { return idLinha; }
    public void setIdLinha(int idLinha) { this.idLinha = idLinha; }
    public int getIdEstacao() { return idEstacao; }
    public void setIdEstacao(int idEstacao) { this.idEstacao = idEstacao; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }
}
