package org.example.cbtc_simulator.view.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cbtc_simulator.dao.*;
import org.example.cbtc_simulator.model.*;
import org.example.cbtc_simulator.exception.OrdemDuplicadaException;
import org.example.cbtc_simulator.exception.EstacaoPossuiVinculoException;
import org.example.cbtc_simulator.exception.CampoObrigatorioException;
import org.example.cbtc_simulator.view.Alerts;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CadastroAssociacaoController implements Initializable {

    @FXML private ComboBox<String> comboLinha;
    @FXML private ComboBox<String> comboSentido;
    @FXML private ComboBox<String> comboEstacao;
    @FXML private TextField fieldOrdem;
    @FXML private TableView<LinhaEstacao> tabela;
    @FXML private TableColumn<LinhaEstacao, String> colLinha;
    @FXML private TableColumn<LinhaEstacao, String> colSentido;
    @FXML private TableColumn<LinhaEstacao, String> colEstacao;
    @FXML private TableColumn<LinhaEstacao, Integer> colOrdem;

    private final LinhaDAO linhaDAO = new LinhaDAO();
    private final EstacaoDAO estacaoDAO = new EstacaoDAO();
    private final LinhaEstacaoDAO leDAO = new LinhaEstacaoDAO();
    private final ObservableList<LinhaEstacao> dados = FXCollections.observableArrayList();
    private Map<Integer, String> linhas;
    private Map<Integer, String> estacoes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboSentido.setItems(FXCollections.observableArrayList("IDA", "VOLTA"));
        comboSentido.setValue("IDA");

        colLinha.setCellValueFactory(c -> new SimpleStringProperty(linhas.get(c.getValue().getIdLinha())));
        colSentido.setCellValueFactory(new PropertyValueFactory<>("sentido"));
        colEstacao.setCellValueFactory(c -> new SimpleStringProperty(estacoes.get(c.getValue().getIdEstacao())));
        colOrdem.setCellValueFactory(new PropertyValueFactory<>("ordem"));

        tabela.setItems(dados);
        carregarCombos();
        listar();
    }

    private void carregarCombos() {
        try {
            linhas = new HashMap<>();
            List<String> nomesLinhas = new ArrayList<>();
            for (Linha l : linhaDAO.listarTodos()) {
                linhas.put(l.getId(), l.getNome());
                nomesLinhas.add(l.getNome());
            }
            Collections.sort(nomesLinhas);
            comboLinha.setItems(FXCollections.observableArrayList(nomesLinhas));

            estacoes = new HashMap<>();
            List<String> nomesEstacoes = new ArrayList<>();
            for (Estacao e : estacaoDAO.listarTodos()) {
                estacoes.put(e.getId(), e.getNome());
                nomesEstacoes.add(e.getNome());
            }
            Collections.sort(nomesEstacoes);
            comboEstacao.setItems(FXCollections.observableArrayList(nomesEstacoes));
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel carregar combos: " + e.getMessage());
        }
    }

    @FXML
    private void adicionar() {
        try {
            String nomeLinha = comboLinha.getValue();
            String sentido = comboSentido.getValue();
            String nomeEstacao = comboEstacao.getValue();
            String ordemStr = fieldOrdem.getText().trim();

            if (nomeLinha == null || sentido == null || nomeEstacao == null || ordemStr.isEmpty()) {
                throw new CampoObrigatorioException("Todos os campos");
            }
            int idLinha = linhas.entrySet().stream()
                    .filter(e -> e.getValue().equals(nomeLinha))
                    .map(Map.Entry::getKey).findFirst().orElse(0);
            int idEstacao = estacoes.entrySet().stream()
                    .filter(e -> e.getValue().equals(nomeEstacao))
                    .map(Map.Entry::getKey).findFirst().orElse(0);
            int ordem;
            try { ordem = Integer.parseInt(ordemStr); }
            catch (NumberFormatException e) { throw new CampoObrigatorioException("Ordem (numero inteiro)"); }

            boolean ordemExiste = dados.stream().anyMatch(le ->
                    le.getIdLinha() == idLinha && le.getSentido().equals(sentido) && le.getOrdem() == ordem);
            if (ordemExiste) {
                throw new OrdemDuplicadaException("linha_estacao");
            }
            LinhaEstacao le = new LinhaEstacao(idLinha, idEstacao, ordem, sentido);
            leDAO.inserir(le);
            limpar();
            listar();
        } catch (CampoObrigatorioException | OrdemDuplicadaException e) {
            Alerts.erro(e.getMessage());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel associar: " + e.getMessage());
        }
    }

    @FXML
    private void excluir() {
        LinhaEstacao sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.erro("Selecione uma associacao");
            return;
        }
        try {
            leDAO.excluir(sel.getId());
            dados.remove(sel);
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel excluir: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        limpar();
    }

    private void listar() {
        try {
            dados.setAll(leDAO.listarTodos());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel listar: " + e.getMessage());
        }
    }

    private void limpar() {
        comboLinha.setValue(null);
        comboEstacao.setValue(null);
        fieldOrdem.clear();
    }
}
