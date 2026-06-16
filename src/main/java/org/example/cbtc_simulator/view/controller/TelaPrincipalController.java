package org.example.cbtc_simulator.view.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.example.cbtc_simulator.dao.*;

import java.io.IOException;
import java.net.URL;

public class TelaPrincipalController {

    @FXML private TabPane tabPane;

    @FXML
    public void initialize() {
        adicionarAba("Estacoes", "cadastro_estacao.fxml");
        adicionarAba("Linhas", "cadastro_linha.fxml");
        adicionarAba("Associacao", "cadastro_associacao.fxml");
        adicionarAba("Trechos", "cadastro_trecho.fxml");
        adicionarAba("Trens", "cadastro_trem.fxml");
        adicionarAba("Viagens", "cadastro_viagem.fxml");
        adicionarAba("Simulacao", "simulacao.fxml");
    }

    private void adicionarAba(String titulo, String fxml) {
        try {
            java.net.URL url = getClass().getResource(fxml);
            if (url == null) {
                Tab tab = new Tab(titulo);
                tab.setContent(new javafx.scene.control.Label("Recurso nao encontrado: " + fxml));
                tabPane.getTabs().add(tab);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Tab tab = new Tab(titulo);
            tab.setContent(loader.load());
            tabPane.getTabs().add(tab);
        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder sb = new StringBuilder("Erro: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                sb.append("\nCausa: ").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
                cause = cause.getCause();
            }
            Tab tab = new Tab(titulo);
            tab.setContent(new javafx.scene.control.Label(sb.toString()));
            tabPane.getTabs().add(tab);
        }
    }
}
