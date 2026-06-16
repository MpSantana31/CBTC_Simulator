package org.example.cbtc_simulator.view;

import javafx.scene.control.Alert;

public final class Alerts {

    private Alerts() {}

    public static void erro(String mensagem) {
        new Alert(Alert.AlertType.ERROR, mensagem).showAndWait();
    }

    public static void info(String mensagem) {
        new Alert(Alert.AlertType.INFORMATION, mensagem).showAndWait();
    }

    public static void confirmarAcao(String titulo, String conteudo, Runnable aoConfirmar) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, conteudo);
        alert.setHeaderText(titulo);
        alert.showAndWait().ifPresent(r -> {
            if (r == javafx.scene.control.ButtonType.OK) aoConfirmar.run();
        });
    }
}
