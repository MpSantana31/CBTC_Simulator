package org.example.cbtc_simulator.view.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cbtc_simulator.dao.LinhaDAO;
import org.example.cbtc_simulator.dao.TremDAO;
import org.example.cbtc_simulator.dao.ViagemProgramadaDAO;
import org.example.cbtc_simulator.exception.CampoObrigatorioException;
import org.example.cbtc_simulator.exception.HorarioPartidaInvalidoException;
import org.example.cbtc_simulator.model.Linha;
import org.example.cbtc_simulator.model.Trem;
import org.example.cbtc_simulator.model.ViagemProgramada;
import org.example.cbtc_simulator.view.Alerts;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CadastroViagemController implements Initializable {

    @FXML private TableView<ViagemProgramada> tabela;
    @FXML private TableColumn<ViagemProgramada, String> colTrem;
    @FXML private TableColumn<ViagemProgramada, String> colLinha;
    @FXML private TableColumn<ViagemProgramada, String> colHorario;
    @FXML private TableColumn<ViagemProgramada, String> colSentido;
    @FXML private TableColumn<ViagemProgramada, String> colRealizada;
    @FXML private ComboBox<String> fieldTrem;
    @FXML private ComboBox<String> fieldLinha;
    @FXML private TextField fieldHorarioPartida;
    @FXML private ComboBox<String> fieldIdaVolta;

    private final ViagemProgramadaDAO dao = new ViagemProgramadaDAO();
    private final TremDAO tremDAO = new TremDAO();
    private final LinhaDAO linhaDAO = new LinhaDAO();
    private final ObservableList<ViagemProgramada> dados = FXCollections.observableArrayList();
    private ViagemProgramada editando;

    private Map<Integer, String> mapTremIdDisplay;
    private Map<String, Integer> mapTremDisplayId;
    private Map<Integer, String> mapLinhaIdNome;
    private Map<String, Integer> mapLinhaNomeId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTrem.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(mapTremIdDisplay.get(cellData.getValue().getIdTrem())));
        colLinha.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(mapLinhaIdNome.get(cellData.getValue().getIdLinha())));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horarioPartida"));
        colSentido.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().isIdaVolta() ? "IDA" : "VOLTA"));
        colRealizada.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().isRealizada() ? "Sim" : "Nao"));
        tabela.setItems(dados);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                fieldTrem.setValue(mapTremIdDisplay.get(sel.getIdTrem()));
                fieldLinha.setValue(mapLinhaIdNome.get(sel.getIdLinha()));
                fieldHorarioPartida.setText(sel.getHorarioPartida());
                fieldIdaVolta.setValue(sel.isIdaVolta() ? "IDA" : "VOLTA");
                editando = sel;
            }
        });
        fieldIdaVolta.setItems(FXCollections.observableArrayList("IDA", "VOLTA"));
        carregarComboBoxes();
        listar();
    }

    private void carregarComboBoxes() {
        try {
            mapTremIdDisplay = new HashMap<>();
            mapTremDisplayId = new HashMap<>();
            for (Trem t : tremDAO.listarTodos()) {
                String display = t.getId() + " - " + t.getModelo();
                mapTremIdDisplay.put(t.getId(), display);
                mapTremDisplayId.put(display, t.getId());
            }
            fieldTrem.setItems(FXCollections.observableArrayList(
                    mapTremIdDisplay.values().stream().sorted().collect(Collectors.toList())));

            mapLinhaIdNome = new HashMap<>();
            mapLinhaNomeId = new HashMap<>();
            for (Linha l : linhaDAO.listarTodos()) {
                mapLinhaIdNome.put(l.getId(), l.getNome());
                mapLinhaNomeId.put(l.getNome(), l.getId());
            }
            fieldLinha.setItems(FXCollections.observableArrayList(
                    mapLinhaIdNome.values().stream().sorted().collect(Collectors.toList())));
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel carregar combos: " + e.getMessage());
        }
    }

    @FXML
    private void salvar() {
        try {
            String tremDisplay = fieldTrem.getValue();
            if (tremDisplay == null) throw new CampoObrigatorioException("Trem");
            String linhaNome = fieldLinha.getValue();
            if (linhaNome == null) throw new CampoObrigatorioException("Linha");
            String horario = fieldHorarioPartida.getText().trim();
            if (horario.isEmpty()) throw new CampoObrigatorioException("Horario de partida");
            if (!horario.matches("\\d{1,2}:\\d{2}")) throw new HorarioPartidaInvalidoException();
            String sentido = fieldIdaVolta.getValue();
            if (sentido == null) throw new CampoObrigatorioException("Sentido");

            int idTrem = mapTremDisplayId.get(tremDisplay);
            int idLinha = mapLinhaNomeId.get(linhaNome);
            boolean idaVolta = "IDA".equals(sentido);

            if (editando == null) {
                dao.inserir(new ViagemProgramada(idTrem, idLinha, horario, idaVolta));
            } else {
                editando.setIdTrem(idTrem);
                editando.setIdLinha(idLinha);
                editando.setHorarioPartida(horario);
                editando.setIdaVolta(idaVolta);
                dao.atualizar(editando);
            }
            limpar();
            listar();
        } catch (CampoObrigatorioException | HorarioPartidaInvalidoException e) {
            Alerts.erro(e.getMessage());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel salvar viagem: " + traduzirErro(e));
        }
    }

    @FXML
    private void editar() {
        ViagemProgramada sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        fieldTrem.setValue(mapTremIdDisplay.get(sel.getIdTrem()));
        fieldLinha.setValue(mapLinhaIdNome.get(sel.getIdLinha()));
        fieldHorarioPartida.setText(sel.getHorarioPartida());
        fieldIdaVolta.setValue(sel.isIdaVolta() ? "IDA" : "VOLTA");
        editando = sel;
    }

    @FXML
    private void excluir() {
        ViagemProgramada sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.erro("Selecione uma viagem para excluir");
            return;
        }
        try {
            dao.excluir(sel.getId());
            dados.remove(sel);
            limpar();
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel excluir: " + traduzirErro(e));
        }
    }

    @FXML
    private void cancelar() {
        limpar();
    }

    private void listar() {
        try {
            dados.setAll(dao.listarTodos());
            carregarComboBoxes();
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel listar viagens: " + traduzirErro(e));
        }
    }

    private void limpar() {
        fieldTrem.setValue(null);
        fieldLinha.setValue(null);
        fieldHorarioPartida.clear();
        fieldIdaVolta.setValue(null);
        editando = null;
        tabela.getSelectionModel().clearSelection();
    }

    private static String traduzirErro(SQLException e) {
        if (e.getMessage() == null) return "erro desconhecido";
        String m = e.getMessage().toLowerCase();
        if (m.contains("foreign key")) return "viagem possui referencias";
        if (m.contains("check")) return "valor invalido";
        return e.getMessage();
    }
}
