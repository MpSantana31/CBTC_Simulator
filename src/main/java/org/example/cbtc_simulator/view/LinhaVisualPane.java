package org.example.cbtc_simulator.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.cbtc_simulator.model.TremEmOperacao;

import java.util.List;

public class LinhaVisualPane extends Pane {

    private List<String> estacoes;
    private List<Double> distanciasAcumuladas;
    private double distanciaTotalKm;
    private String mensagem;
    private Color corLinha = Color.BLACK;

    public void configurarLinha(List<String> nomesEstacoes, List<Double> distanciasKm, String corHex) {
        this.estacoes = nomesEstacoes;
        this.distanciasAcumuladas = distanciasKm;
        this.distanciaTotalKm = distanciasKm.isEmpty() ? 0 : distanciasKm.get(distanciasKm.size() - 1);
        this.mensagem = null;
        try {
            this.corLinha = Color.web(corHex);
        } catch (Exception e) {
            this.corLinha = Color.BLACK;
        }
    }

    public void setMensagem(String msg) {
        this.mensagem = msg;
    }

    public List<String> getEstacoes() {
        return estacoes;
    }

    public void desenhar(List<TremEmOperacao> trens) {
        getChildren().clear();

        double w = Math.max(getWidth(), 200);
        double h = Math.max(getHeight(), 200);

        if (estacoes == null || estacoes.size() < 2) {
            Text aviso = new Text(w / 2 - 100, h / 2, mensagem != null ? mensagem : "Nenhuma linha configurada");
            aviso.setFont(Font.font(14));
            aviso.setFill(Color.GRAY);
            getChildren().add(aviso);
            return;
        }

        double margem = 50;
        double areaUtil = w - 2 * margem;
        double yIda = h / 2 - 20;
        double yVolta = h / 2 + 20;
        double passoX = estacoes.size() > 1 ? areaUtil / (estacoes.size() - 1) : 0;

        for (int i = 0; i < estacoes.size(); i++) {
            double x = margem + i * passoX;

            Circle estIda = new Circle(x, yIda, 10, corLinha);
            estIda.setStroke(Color.BLACK);
            estIda.setStrokeWidth(2);
            Text labelIda = new Text(x - 15, yIda - 18, estacoes.get(i));
            labelIda.setFont(Font.font(11));

            Circle estVolta = new Circle(x, yVolta, 10, corLinha);
            estVolta.setStroke(Color.BLACK);
            estVolta.setStrokeWidth(2);
            Text labelVolta = new Text(x - 15, yVolta + 30, estacoes.get(i));
            labelVolta.setFont(Font.font(11));

            getChildren().addAll(estIda, labelIda, estVolta, labelVolta);

            if (i < estacoes.size() - 1) {
                double x2 = margem + (i + 1) * passoX;
                Line trilhoIda = new Line(x, yIda, x2, yIda);
                trilhoIda.setStroke(corLinha);
                trilhoIda.setStrokeWidth(4);

                Line trilhoVolta = new Line(x2, yVolta, x, yVolta);
                trilhoVolta.setStroke(Color.GRAY);
                trilhoVolta.setStrokeWidth(3);
                trilhoVolta.getStrokeDashArray().setAll(10.0, 8.0);

                getChildren().addAll(trilhoIda, trilhoVolta);
            }
        }

        Text labelIda = new Text(8, yIda + 5, "IDA");
        labelIda.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        Text labelVolta = new Text(8, yVolta + 5, "VOLTA");
        labelVolta.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        getChildren().addAll(labelIda, labelVolta);

        for (TremEmOperacao trem : trens) {
            if (trem.getTrechoOrdemAtual() >= estacoes.size() - 1) continue;
            double y = "IDA".equals(trem.getDirecao()) ? yIda : yVolta;
            double xInicio = margem + trem.getTrechoOrdemAtual() * passoX;
            double xFim = margem + (trem.getTrechoOrdemAtual() + 1) * passoX;
            double proporcao = distanciaTotalKm > 0
                    ? Math.min(1, trem.getProgressoKm() / (distanciaTotalKm / Math.max(1, estacoes.size() - 1)))
                    : 0;
            double xTrem = xInicio + (xFim - xInicio) * proporcao;
            Rectangle rect = new Rectangle(xTrem - 8, y - 6, 16, 12);
            rect.setFill("IDA".equals(trem.getDirecao()) ? Color.CORNFLOWERBLUE : Color.ORANGE);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1.5);
            getChildren().add(rect);
        }
    }
}
