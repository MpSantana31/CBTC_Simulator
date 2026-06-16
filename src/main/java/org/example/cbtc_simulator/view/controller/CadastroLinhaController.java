package org.example.cbtc_simulator.view.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cbtc_simulator.dao.LinhaDAO;
import org.example.cbtc_simulator.exception.CampoObrigatorioException;
import org.example.cbtc_simulator.exception.FKViolationException;
import org.example.cbtc_simulator.exception.LinhaSemNomeException;
import org.example.cbtc_simulator.model.Linha;
import org.example.cbtc_simulator.view.Alerts;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CadastroLinhaController implements Initializable {

    @FXML private TableView<Linha> tabela;
    @FXML private TableColumn<Linha, String> colNome;
    @FXML private TableColumn<Linha, String> colCor;
    @FXML private TableColumn<Linha, Double> colVelMax;
    @FXML private TableColumn<Linha, String> colTipoVia;
    @FXML private TextField fieldNome;
    @FXML private TextField fieldCorHex;
    @FXML private TextField fieldVelocidadeMaxKmh;
    @FXML private ComboBox<String> fieldTipoVia;

    private final LinhaDAO dao = new LinhaDAO();
    private final ObservableList<Linha> dados = FXCollections.observableArrayList();
    private Linha editando;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCor.setCellValueFactory(new PropertyValueFactory<>("corHex"));
        colVelMax.setCellValueFactory(new PropertyValueFactory<>("velocidadeMaxKmh"));
        colTipoVia.setCellValueFactory(new PropertyValueFactory<>("tipoVia"));
        tabela.setItems(dados);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                fieldNome.setText(sel.getNome());
                fieldCorHex.setText(sel.getCorHex());
                fieldVelocidadeMaxKmh.setText(String.valueOf(sel.getVelocidadeMaxKmh()));
                fieldTipoVia.setValue(sel.getTipoVia());
                editando = sel;
            }
        });
        fieldTipoVia.setItems(FXCollections.observableArrayList("SUPERFICIE", "ELEVADA", "SUBTERRANEA"));
        listar();
    }

    @FXML
    private void salvar() {
        try {
            String nome = fieldNome.getText().trim();
            if (nome.isEmpty()) throw new LinhaSemNomeException();
            String cor = fieldCorHex.getText().trim();
            if (cor.isEmpty()) throw new CampoObrigatorioException("Cor hexadecimal");
            double vel;
            try {
                vel = Double.parseDouble(fieldVelocidadeMaxKmh.getText().trim());
            } catch (NumberFormatException e) {
                throw new CampoObrigatorioException("Velocidade maxima (numero valido)");
            }
            String tipo = fieldTipoVia.getValue();
            if (tipo == null) throw new CampoObrigatorioException("Tipo de via");

            if (editando == null) {
                dao.inserir(new Linha(nome, cor, vel, tipo));
            } else {
                editando.setNome(nome);
                editando.setCorHex(cor);
                editando.setVelocidadeMaxKmh(vel);
                editando.setTipoVia(tipo);
                dao.atualizar(editando);
            }
            limpar();
            listar();
        } catch (LinhaSemNomeException | CampoObrigatorioException e) {
            Alerts.erro(e.getMessage());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel salvar: " + traduzirErro(e));
        }
    }

    @FXML
    private void editar() {
        Linha sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        fieldNome.setText(sel.getNome());
        fieldCorHex.setText(sel.getCorHex());
        fieldVelocidadeMaxKmh.setText(String.valueOf(sel.getVelocidadeMaxKmh()));
        fieldTipoVia.setValue(sel.getTipoVia());
        editando = sel;
    }

    @FXML
    private void excluir() {
        Linha sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.erro("Selecione uma linha para excluir");
            return;
        }
        try {
            dao.excluir(sel.getId());
            dados.remove(sel);
            limpar();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key")) {
                throw new FKViolationException("linha possui trechos/viagens/associacoes vinculadas");
            }
            Alerts.erro("Nao foi possivel excluir: " + traduzirErro(e));
        }
    }

    @FXML
    private void cancelar() { limpar(); }

    private void listar() {
        try {
            dados.setAll(dao.listarTodos());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel listar linhas: " + traduzirErro(e));
        }
    }

    private void limpar() {
        fieldNome.clear();
        fieldCorHex.clear();
        fieldVelocidadeMaxKmh.clear();
        fieldTipoVia.setValue(null);
        editando = null;
        tabela.getSelectionModel().clearSelection();
    }

    private static String traduzirErro(SQLException e) {
        if (e.getMessage() == null) return "erro desconhecido";
        String m = e.getMessage().toLowerCase();
        if (m.contains("foreign key")) return "linha possui vinculos";
        if (m.contains("unique") || m.contains("duplicate")) return "linha ja cadastrada";
        if (m.contains("check")) return "valor invalido (verifique tipo de via)";
        return e.getMessage();
    }
}
