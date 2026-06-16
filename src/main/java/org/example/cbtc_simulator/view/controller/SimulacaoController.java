package org.example.cbtc_simulator.view.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.example.cbtc_simulator.dao.*;
import org.example.cbtc_simulator.model.*;
import org.example.cbtc_simulator.service.SimuladorCBTCService;
import org.example.cbtc_simulator.view.LinhaVisualPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SimulacaoController implements Initializable {

    @FXML private TableView<TremEmOperacao> tabelaStatus;
    @FXML private TableColumn<TremEmOperacao, Integer> colTrem;
    @FXML private TableColumn<TremEmOperacao, String> colSentido;
    @FXML private TableColumn<TremEmOperacao, Double> colVeloc;
    @FXML private TableColumn<TremEmOperacao, StatusTrem> colStatus;
    @FXML private TableColumn<TremEmOperacao, String> colTrecho;
    @FXML private TableColumn<TremEmOperacao, Double> colProgresso;
    @FXML private TableColumn<TremEmOperacao, Double> colKmLinha;
    @FXML private TableColumn<TremEmOperacao, Double> colMaxVeloc;
    @FXML private TableColumn<TremEmOperacao, Double> colMA;
    @FXML private TableColumn<TremEmOperacao, Double> colHeadway;
    @FXML private TableColumn<TremEmOperacao, String> colTremAtras;
    @FXML private TableColumn<TremEmOperacao, String> colTremFrente;
    @FXML private TableColumn<TremEmOperacao, String> colETA;
    @FXML private TextArea logArea;
    @FXML private StackPane visualPane;
    @FXML private Label labelRelogio;
    @FXML private Label labelAviso;
    @FXML private Button btnRodar;
    @FXML private Button btnParar;
    @FXML private ComboBox<String> comboVel;

    private SimuladorCBTCService simulador;
    private final ObservableList<TremEmOperacao> dadosTrens = FXCollections.observableArrayList();
    private int ultimoEventoLido;
    private LinhaVisualPane linhaVisual;
    private int totalViagensPendentes;
    private Timeline timeline;
    private double velocidadeSimulacao = 1.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTrem.setCellValueFactory(new PropertyValueFactory<>("idTrem"));
        colSentido.setCellValueFactory(new PropertyValueFactory<>("direcao"));
        colVeloc.setCellValueFactory(new PropertyValueFactory<>("velocidadeAtualKmh"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colProgresso.setCellValueFactory(new PropertyValueFactory<>("progressoKm"));
        colMaxVeloc.setCellValueFactory(new PropertyValueFactory<>("velocidadeMax"));

        colTrecho.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Trecho " + (cellData.getValue().getTrechoOrdemAtual() + 1)));
        colTremAtras.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getTremAtras(cellData.getValue().getIdTrem())));
        colTremFrente.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getTremFrente(cellData.getValue().getIdTrem())));
        colETA.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getETA(cellData.getValue().getIdTrem())));
        colMA.setCellValueFactory(cellData -> {
            try { return new javafx.beans.property.ReadOnlyObjectWrapper<>(simulador.calcularMA(cellData.getValue().getIdTrem())); }
            catch (Exception e) { return new javafx.beans.property.ReadOnlyObjectWrapper<>(0.0); }
        });
        colHeadway.setCellValueFactory(cellData -> {
            try { return new javafx.beans.property.ReadOnlyObjectWrapper<>(simulador.calcularHeadway(cellData.getValue().getIdTrem())); }
            catch (Exception e) { return new javafx.beans.property.ReadOnlyObjectWrapper<>(0.0); }
        });
        colKmLinha.setCellValueFactory(cellData -> {
            try { return new javafx.beans.property.ReadOnlyObjectWrapper<>(simulador.getKmNaLinha(cellData.getValue().getIdTrem())); }
            catch (Exception e) { return new javafx.beans.property.ReadOnlyObjectWrapper<>(0.0); }
        });

        colVeloc.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.1f", v));
            }
        });
        colProgresso.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.2f", v));
            }
        });
        colMaxVeloc.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.0f", v));
            }
        });
        colMA.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.2f", v));
            }
        });
        colHeadway.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.2f", v));
            }
        });
        colKmLinha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.2f", v));
            }
        });

        tabelaStatus.setItems(dadosTrens);

        linhaVisual = new LinhaVisualPane();
        linhaVisual.prefWidthProperty().bind(visualPane.widthProperty());
        linhaVisual.prefHeightProperty().bind(visualPane.heightProperty());
        visualPane.getChildren().add(linhaVisual);

        comboVel.setItems(FXCollections.observableArrayList(
                "0.5x", "1x", "2x", "5x", "10x"));
        comboVel.setValue("1x");
        comboVel.setOnAction(e -> {
            switch (comboVel.getValue()) {
                case "0.5x": velocidadeSimulacao = 0.5; break;
                case "1x":   velocidadeSimulacao = 1.0; break;
                case "2x":   velocidadeSimulacao = 2.0; break;
                case "5x":   velocidadeSimulacao = 5.0; break;
                case "10x":  velocidadeSimulacao = 10.0; break;
            }
            if (timeline != null) timeline.setRate(velocidadeSimulacao);
        });

        try {
            carregarDadosLinha();
            TrechoDAO trechoDAO = new TrechoDAO();
            TremDAO tremDAO = new TremDAO();
            ViagemProgramadaDAO viagemDAO = new ViagemProgramadaDAO();
            simulador = new SimuladorCBTCService(trechoDAO, tremDAO, viagemDAO);
            simulador.iniciarSimulacao();
            totalViagensPendentes = viagemDAO.listarPendentes().size();
            if (totalViagensPendentes == 0) {
                logArea.appendText("Nenhuma viagem programada. Cadastre uma viagem na aba Viagens.\n");
                logArea.appendText("Apos cadastrar, clique em Tick para iniciar a simulacao.\n");
            }
            atualizarTela();
        } catch (SQLException e) {
            logArea.appendText("Erro ao iniciar simulacao: " + e.getMessage() + "\n");
        }
    }

    private void carregarDadosLinha() throws SQLException {
        LinhaDAO linhaDAO = new LinhaDAO();
        LinhaEstacaoDAO leDAO = new LinhaEstacaoDAO();
        EstacaoDAO estDAO = new EstacaoDAO();
        TrechoDAO trDAO = new TrechoDAO();

        Map<Integer, String> estacoes = new HashMap<>();
        for (Estacao e : estDAO.listarTodos()) {
            estacoes.put(e.getId(), e.getNome());
        }

        List<Linha> linhas = linhaDAO.listarTodos();
        if (linhas.isEmpty()) {
            linhaVisual.setMensagem("Cadastre uma linha e estacoes primeiro");
            return;
        }

        int idLinha = linhas.get(0).getId();
        String corHex = linhas.get(0).getCorHex();
        List<LinhaEstacao> ida = leDAO.listarPorLinhaESentido(idLinha, "IDA");

        if (!ida.isEmpty()) {
            List<String> nomes = ida.stream()
                    .map(le -> estacoes.get(le.getIdEstacao()))
                    .collect(Collectors.toList());
            List<Trecho> trechos = trDAO.listarPorLinhaESentido(idLinha, "IDA");
            List<Double> acumuladas = new ArrayList<>();
            double acc = 0;
            acumuladas.add(0.0);
            for (Trecho t : trechos) {
                acc += t.getDistanciaKm();
                acumuladas.add(acc);
            }
            linhaVisual.configurarLinha(nomes, acumuladas, corHex);
        } else {
            List<Trecho> trechos = trDAO.listarPorLinhaESentido(idLinha, "IDA");
            if (!trechos.isEmpty() && estacoes.containsKey(trechos.get(0).getIdEstacaoOrigem())) {
                List<String> nomes = new ArrayList<>();
                List<Double> acumuladas = new ArrayList<>();
                double acc = 0;
                acumuladas.add(0.0);
                nomes.add(estacoes.get(trechos.get(0).getIdEstacaoOrigem()));
                for (Trecho t : trechos) {
                    nomes.add(estacoes.get(t.getIdEstacaoDestino()));
                    acc += t.getDistanciaKm();
                    acumuladas.add(acc);
                }
                linhaVisual.configurarLinha(nomes, acumuladas, corHex);
            } else {
                linhaVisual.setMensagem("Associe estacoes a linha (LinhaEstacao) ou cadastre trechos");
            }
        }
    }

    @FXML
    private void avancarTick() {
        if (simulador == null) return;
        try {
            simulador.executarTick();
            atualizarTela();
        } catch (SQLException e) {
            logArea.appendText("Erro no tick: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void rodarAuto() {
        if (simulador == null) return;
        if (timeline != null) return;
        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            try {
                simulador.executarTick();
                atualizarTela();
            } catch (SQLException ex) {
                logArea.appendText("Erro no tick: " + ex.getMessage() + "\n");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setRate(velocidadeSimulacao);
        timeline.play();
        btnRodar.setDisable(true);
        btnParar.setDisable(false);
        logArea.appendText("--- Simulacao iniciada (auto) ---\n");
    }

    @FXML
    private void pararAuto() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        btnRodar.setDisable(false);
        btnParar.setDisable(true);
        logArea.appendText("--- Simulacao pausada ---\n");
    }

    @FXML
    private void resetTudo() {
        pararAuto();
        if (simulador == null) return;
        try {
            simulador.resetTudo();
            simulador.iniciarSimulacao();
            ultimoEventoLido = 0;
            logArea.clear();
            atualizarTela();
        } catch (SQLException e) {
            logArea.appendText("Erro no reset: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void resetSoTrens() {
        pararAuto();
        if (simulador == null) return;
        simulador.resetSoTrens();
        ultimoEventoLido = 0;
        atualizarTela();
    }

    private String getTremAtras(int idTrem) {
        if (simulador == null) return "-";
        return simulador.getTremAtras(idTrem);
    }

    private String getTremFrente(int idTrem) {
        if (simulador == null) return "-";
        return simulador.getTremFrente(idTrem);
    }

    private String getETA(int idTrem) {
        if (simulador == null) return "-";
        try { return simulador.getETA(idTrem); }
        catch (SQLException e) { return "-"; }
    }

    private void atualizarTela() {
        var ativos = simulador.getTrensAtivos();
        dadosTrens.setAll(ativos);

        double seg = simulador.getTempoSimuladoSegundos();
        int h = (int) seg / 3600;
        int m = (int) (seg % 3600) / 60;
        int s = (int) (seg % 60);
        labelRelogio.setText(String.format("Tempo: %02d:%02d:%02d  |  Trens ativos: %d  |  Vel: %sx",
                h, m, s, ativos.size(), comboVel.getValue()));

        if (totalViagensPendentes == 0 && ativos.isEmpty()) {
            try {
                totalViagensPendentes = new ViagemProgramadaDAO().listarPendentes().size();
            } catch (SQLException ignored) {}
        }

        var eventos = simulador.getEventos();
        for (int i = ultimoEventoLido; i < eventos.size(); i++) {
            EventoSimulacao ev = eventos.get(i);
            logArea.appendText(String.format("[Tick %d] Trem %d %s: %s%n",
                    ev.getTick(), ev.getIdTrem(), ev.getTipoEvento(), ev.getMensagem()));
        }
        ultimoEventoLido = eventos.size();

        linhaVisual.desenhar(ativos);
        boolean temLinha = linhaVisual.getEstacoes() != null;
        labelAviso.setVisible(!temLinha);
    }
}
