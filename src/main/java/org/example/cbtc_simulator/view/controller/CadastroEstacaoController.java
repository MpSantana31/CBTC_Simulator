package org.example.cbtc_simulator.view.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cbtc_simulator.dao.EstacaoDAO;
import org.example.cbtc_simulator.exception.EstacaoPossuiVinculoException;
import org.example.cbtc_simulator.exception.EstacaoSemNomeException;
import org.example.cbtc_simulator.model.Estacao;
import org.example.cbtc_simulator.view.Alerts;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CadastroEstacaoController implements Initializable {

    @FXML private TableView<Estacao> tabela;
    @FXML private TableColumn<Estacao, String> colNome;
    @FXML private TableColumn<Estacao, String> colZona;
    @FXML private TextField fieldNome;
    @FXML private TextField fieldZona;

    private final EstacaoDAO dao = new EstacaoDAO();
    private final ObservableList<Estacao> dados = FXCollections.observableArrayList();
    private Estacao editando;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colZona.setCellValueFactory(new PropertyValueFactory<>("zona"));
        tabela.setItems(dados);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                fieldNome.setText(sel.getNome());
                fieldZona.setText(sel.getZona());
                editando = sel;
            }
        });
        listar();
    }

    @FXML
    private void salvar() {
        try {
            String nome = fieldNome.getText().trim();
            if (nome.isEmpty()) throw new EstacaoSemNomeException();
            if (editando == null) {
                dao.inserir(new Estacao(nome, fieldZona.getText().trim()));
            } else {
                editando.setNome(nome);
                editando.setZona(fieldZona.getText().trim());
                dao.atualizar(editando);
            }
            limpar();
            listar();
        } catch (EstacaoSemNomeException e) {
            Alerts.erro(e.getMessage());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel salvar: " + mensagemAmigavel(e));
        }
    }

    @FXML
    private void editar() {
        Estacao sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        fieldNome.setText(sel.getNome());
        fieldZona.setText(sel.getZona());
        editando = sel;
    }

    @FXML
    private void excluir() {
        Estacao sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.erro("Selecione uma estacao para excluir");
            return;
        }
        try {
            dao.excluir(sel.getId());
            dados.remove(sel);
            limpar();
        } catch (EstacaoPossuiVinculoException e) {
            Alerts.erro(e.getMessage());
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key")) {
                throw new EstacaoPossuiVinculoException("possui trechos ou associacoes");
            }
            Alerts.erro("Nao foi possivel excluir: " + mensagemAmigavel(e));
        }
    }

    @FXML
    private void cancelar() {
        limpar();
    }

    private void listar() {
        try {
            dados.setAll(dao.listarTodos());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel listar estacoes: " + mensagemAmigavel(e));
        }
    }

    private void limpar() {
        fieldNome.clear();
        fieldZona.clear();
        editando = null;
        tabela.getSelectionModel().clearSelection();
    }

    private static String mensagemAmigavel(SQLException e) {
        if (e.getMessage() == null) return "erro desconhecido";
        String m = e.getMessage().toLowerCase();
        if (m.contains("foreign key")) return "estacao possui vinculos (trechos/associacoes)";
        if (m.contains("unique") || m.contains("duplicate")) return "nome ja cadastrado";
        if (m.contains("null")) return "campo obrigatorio nao preenchido";
        return e.getMessage();
    }
}
