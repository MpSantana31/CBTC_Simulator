package org.example.cbtc_simulator.view.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cbtc_simulator.dao.EstacaoDAO;
import org.example.cbtc_simulator.dao.LinhaDAO;
import org.example.cbtc_simulator.dao.TrechoDAO;
import org.example.cbtc_simulator.exception.CampoObrigatorioException;
import org.example.cbtc_simulator.model.Estacao;
import org.example.cbtc_simulator.model.Linha;
import org.example.cbtc_simulator.model.Trecho;
import org.example.cbtc_simulator.view.Alerts;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CadastroTrechoController implements Initializable {

    @FXML private TableView<Trecho> tabela;
    @FXML private TableColumn<Trecho, String> colLinha;
    @FXML private TableColumn<Trecho, String> colSentido;
    @FXML private TableColumn<Trecho, String> colOrigem;
    @FXML private TableColumn<Trecho, String> colDestino;
    @FXML private TableColumn<Trecho, Double> colDistancia;
    @FXML private TableColumn<Trecho, Integer> colOrdem;
    @FXML private ComboBox<String> fieldLinha;
    @FXML private ComboBox<String> fieldSentido;
    @FXML private ComboBox<String> fieldEstacaoOrigem;
    @FXML private ComboBox<String> fieldEstacaoDestino;
    @FXML private TextField fieldDistanciaKm;
    @FXML private TextField fieldOrdem;

    private final TrechoDAO dao = new TrechoDAO();
    private final LinhaDAO linhaDAO = new LinhaDAO();
    private final EstacaoDAO estacaoDAO = new EstacaoDAO();
    private final ObservableList<Trecho> dados = FXCollections.observableArrayList();
    private Trecho editando;

    private Map<Integer, String> mapLinhaIdNome;
    private Map<String, Integer> mapLinhaNomeId;
    private Map<Integer, String> mapEstacaoIdNome;
    private Map<String, Integer> mapEstacaoNomeId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colLinha.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(mapLinhaIdNome.get(cellData.getValue().getIdLinha())));
        colSentido.setCellValueFactory(new PropertyValueFactory<>("sentido"));
        colOrigem.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(mapEstacaoIdNome.get(cellData.getValue().getIdEstacaoOrigem())));
        colDestino.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(mapEstacaoIdNome.get(cellData.getValue().getIdEstacaoDestino())));
        colDistancia.setCellValueFactory(new PropertyValueFactory<>("distanciaKm"));
        colOrdem.setCellValueFactory(new PropertyValueFactory<>("ordem"));
        tabela.setItems(dados);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                fieldLinha.setValue(mapLinhaIdNome.get(sel.getIdLinha()));
                fieldSentido.setValue(sel.getSentido());
                fieldEstacaoOrigem.setValue(mapEstacaoIdNome.get(sel.getIdEstacaoOrigem()));
                fieldEstacaoDestino.setValue(mapEstacaoIdNome.get(sel.getIdEstacaoDestino()));
                fieldDistanciaKm.setText(String.valueOf(sel.getDistanciaKm()));
                fieldOrdem.setText(String.valueOf(sel.getOrdem()));
                editando = sel;
            }
        });
        fieldSentido.setItems(FXCollections.observableArrayList("IDA", "VOLTA"));
        carregarComboBoxes();
        listar();
    }

    private void carregarComboBoxes() {
        try {
            mapLinhaIdNome = new HashMap<>();
            mapLinhaNomeId = new HashMap<>();
            for (Linha l : linhaDAO.listarTodos()) {
                mapLinhaIdNome.put(l.getId(), l.getNome());
                mapLinhaNomeId.put(l.getNome(), l.getId());
            }
            fieldLinha.setItems(FXCollections.observableArrayList(
                    mapLinhaIdNome.values().stream().sorted().collect(Collectors.toList())));

            mapEstacaoIdNome = new HashMap<>();
            mapEstacaoNomeId = new HashMap<>();
            for (Estacao e : estacaoDAO.listarTodos()) {
                mapEstacaoIdNome.put(e.getId(), e.getNome());
                mapEstacaoNomeId.put(e.getNome(), e.getId());
            }
            java.util.List<String> estacoesOrdenadas = mapEstacaoIdNome.values().stream()
                    .sorted().collect(Collectors.toList());
            fieldEstacaoOrigem.setItems(FXCollections.observableArrayList(estacoesOrdenadas));
            fieldEstacaoDestino.setItems(FXCollections.observableArrayList(estacoesOrdenadas));
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel carregar combos: " + traduzirErro(e));
        }
    }

    @FXML
    private void salvar() {
        try {
            String linhaNome = fieldLinha.getValue();
            if (linhaNome == null) throw new CampoObrigatorioException("Linha");
            String sentido = fieldSentido.getValue();
            if (sentido == null) throw new CampoObrigatorioException("Sentido");
            String origemNome = fieldEstacaoOrigem.getValue();
            if (origemNome == null) throw new CampoObrigatorioException("Estacao de origem");
            String destinoNome = fieldEstacaoDestino.getValue();
            if (destinoNome == null) throw new CampoObrigatorioException("Estacao de destino");
            if (origemNome.equals(destinoNome)) throw new CampoObrigatorioException("Origem e destino devem ser diferentes");
            double dist;
            try { dist = Double.parseDouble(fieldDistanciaKm.getText().trim()); }
            catch (NumberFormatException e) { throw new CampoObrigatorioException("Distancia (numero valido)"); }
            int ordem;
            try { ordem = Integer.parseInt(fieldOrdem.getText().trim()); }
            catch (NumberFormatException e) { throw new CampoObrigatorioException("Ordem (numero inteiro)"); }

            int idLinha = mapLinhaNomeId.get(linhaNome);
            int idOrigem = mapEstacaoNomeId.get(origemNome);
            int idDestino = mapEstacaoNomeId.get(destinoNome);
            try {
                if (editando == null) {
                    dao.inserir(new Trecho(idLinha, sentido, idOrigem, idDestino, dist, ordem));
                } else {
                    editando.setIdLinha(idLinha);
                    editando.setSentido(sentido);
                    editando.setIdEstacaoOrigem(idOrigem);
                    editando.setIdEstacaoDestino(idDestino);
                    editando.setDistanciaKm(dist);
                    editando.setOrdem(ordem);
                    dao.atualizar(editando);
                }
                limpar();
                listar();
            } catch (SQLException e) {
                Alerts.erro("Nao foi possivel salvar trecho: " + traduzirErro(e));
            }
        } catch (CampoObrigatorioException e) {
            Alerts.erro(e.getMessage());
        }
    }

    @FXML
    private void editar() {
        Trecho sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        fieldLinha.setValue(mapLinhaIdNome.get(sel.getIdLinha()));
        fieldSentido.setValue(sel.getSentido());
        fieldEstacaoOrigem.setValue(mapEstacaoIdNome.get(sel.getIdEstacaoOrigem()));
        fieldEstacaoDestino.setValue(mapEstacaoIdNome.get(sel.getIdEstacaoDestino()));
        fieldDistanciaKm.setText(String.valueOf(sel.getDistanciaKm()));
        fieldOrdem.setText(String.valueOf(sel.getOrdem()));
        editando = sel;
    }

    @FXML
    private void excluir() {
        Trecho sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.erro("Selecione um trecho para excluir");
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
            Alerts.erro("Nao foi possivel listar trechos: " + traduzirErro(e));
        }
    }

    private void limpar() {
        fieldLinha.setValue(null);
        fieldSentido.setValue(null);
        fieldEstacaoOrigem.setValue(null);
        fieldEstacaoDestino.setValue(null);
        fieldDistanciaKm.clear();
        fieldOrdem.clear();
        editando = null;
        tabela.getSelectionModel().clearSelection();
    }

    private static String traduzirErro(SQLException e) {
        if (e.getMessage() == null) return "erro desconhecido";
        String m = e.getMessage().toLowerCase();
        if (m.contains("foreign key")) return "trecho possui referencias";
        if (m.contains("unique") || m.contains("duplicate")) return "trecho duplicado";
        if (m.contains("check")) return "valor invalido (origem != destino, distancia > 0)";
        return e.getMessage();
    }
}
