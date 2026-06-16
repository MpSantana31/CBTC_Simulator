package org.example.cbtc_simulator.view.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.cbtc_simulator.dao.TremDAO;
import org.example.cbtc_simulator.exception.CampoObrigatorioException;
import org.example.cbtc_simulator.exception.FKViolationException;
import org.example.cbtc_simulator.model.Trem;
import org.example.cbtc_simulator.view.Alerts;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CadastroTremController implements Initializable {

    @FXML private TableView<Trem> tabela;
    @FXML private TableColumn<Trem, String> colModelo;
    @FXML private TableColumn<Trem, Double> colVelMax;
    @FXML private TableColumn<Trem, Double> colAceleracao;
    @FXML private TableColumn<Trem, Integer> colCapacidade;
    @FXML private TableColumn<Trem, Double> colComprimento;
    @FXML private TextField fieldModelo;
    @FXML private TextField fieldVelocidadeMaxKmh;
    @FXML private TextField fieldAceleracaoMs2;
    @FXML private TextField fieldCapacidade;
    @FXML private TextField fieldComprimentoM;

    private final TremDAO dao = new TremDAO();
    private final ObservableList<Trem> dados = FXCollections.observableArrayList();
    private Trem editando;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colVelMax.setCellValueFactory(new PropertyValueFactory<>("velocidadeMaxKmh"));
        colAceleracao.setCellValueFactory(new PropertyValueFactory<>("aceleracaoMs2"));
        colCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacidade"));
        colComprimento.setCellValueFactory(new PropertyValueFactory<>("comprimentoM"));
        tabela.setItems(dados);
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                fieldModelo.setText(sel.getModelo());
                fieldVelocidadeMaxKmh.setText(String.valueOf(sel.getVelocidadeMaxKmh()));
                fieldAceleracaoMs2.setText(String.valueOf(sel.getAceleracaoMs2()));
                fieldCapacidade.setText(String.valueOf(sel.getCapacidade()));
                fieldComprimentoM.setText(String.valueOf(sel.getComprimentoM()));
                editando = sel;
            }
        });
        listar();
    }

    @FXML
    private void salvar() {
        try {
            String modelo = fieldModelo.getText().trim();
            if (modelo.isEmpty()) throw new CampoObrigatorioException("Modelo");
            double vel = parseDouble(fieldVelocidadeMaxKmh.getText(), "Velocidade maxima");
            double acel = parseDouble(fieldAceleracaoMs2.getText(), "Aceleracao");
            int cap = parseInt(fieldCapacidade.getText(), "Capacidade");
            double comp = parseDouble(fieldComprimentoM.getText(), "Comprimento");

            if (editando == null) {
                dao.inserir(new Trem(modelo, vel, acel, cap, comp));
            } else {
                editando.setModelo(modelo);
                editando.setVelocidadeMaxKmh(vel);
                editando.setAceleracaoMs2(acel);
                editando.setCapacidade(cap);
                editando.setComprimentoM(comp);
                dao.atualizar(editando);
            }
            limpar();
            listar();
        } catch (CampoObrigatorioException e) {
            Alerts.erro(e.getMessage());
        } catch (SQLException e) {
            Alerts.erro("Nao foi possivel salvar: " + traduzirErro(e));
        }
    }

    @FXML private void editar() {
        Trem sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        fieldModelo.setText(sel.getModelo());
        fieldVelocidadeMaxKmh.setText(String.valueOf(sel.getVelocidadeMaxKmh()));
        fieldAceleracaoMs2.setText(String.valueOf(sel.getAceleracaoMs2()));
        fieldCapacidade.setText(String.valueOf(sel.getCapacidade()));
        fieldComprimentoM.setText(String.valueOf(sel.getComprimentoM()));
        editando = sel;
    }

    @FXML
    private void excluir() {
        Trem sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alerts.erro("Selecione um trem para excluir");
            return;
        }
        try {
            dao.excluir(sel.getId());
            dados.remove(sel);
            limpar();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key")) {
                throw new FKViolationException("trem possui viagens vinculadas");
            }
            Alerts.erro("Nao foi possivel excluir: " + traduzirErro(e));
        }
    }

    @FXML private void cancelar() { limpar(); }

    private void listar() {
        try { dados.setAll(dao.listarTodos()); }
        catch (SQLException e) { Alerts.erro("Nao foi possivel listar trens: " + traduzirErro(e)); }
    }

    private void limpar() {
        fieldModelo.clear();
        fieldVelocidadeMaxKmh.clear();
        fieldAceleracaoMs2.clear();
        fieldCapacidade.clear();
        fieldComprimentoM.clear();
        editando = null;
        tabela.getSelectionModel().clearSelection();
    }

    private static double parseDouble(String s, String campo) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { throw new CampoObrigatorioException(campo); }
    }

    private static int parseInt(String s, String campo) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { throw new CampoObrigatorioException(campo); }
    }

    private static String traduzirErro(SQLException e) {
        if (e.getMessage() == null) return "erro desconhecido";
        String m = e.getMessage().toLowerCase();
        if (m.contains("foreign key")) return "trem possui viagens vinculadas";
        if (m.contains("check")) return "valor invalido (velocidade > 0)";
        return e.getMessage();
    }
}
