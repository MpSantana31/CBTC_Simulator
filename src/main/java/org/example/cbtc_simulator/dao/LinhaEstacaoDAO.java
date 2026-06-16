package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.LinhaEstacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinhaEstacaoDAO implements DAO<LinhaEstacao> {
    @Override
    public void inserir(LinhaEstacao le) throws SQLException {
        String sql = "INSERT INTO linha_estacao (id_linha, id_estacao, ordem, sentido) VALUES (?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, le.getIdLinha());
            stmt.setInt(2, le.getIdEstacao());
            stmt.setInt(3, le.getOrdem());
            stmt.setString(4, le.getSentido());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) le.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Optional<LinhaEstacao> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM linha_estacao WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<LinhaEstacao> listarTodos() throws SQLException {
        List<LinhaEstacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM linha_estacao ORDER BY id_linha, sentido, ordem";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<LinhaEstacao> listarPorLinhaESentido(int idLinha, String sentido) throws SQLException {
        List<LinhaEstacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM linha_estacao WHERE id_linha = ? AND sentido = ? ORDER BY ordem";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, idLinha);
            stmt.setString(2, sentido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }

    @Override
    public void atualizar(LinhaEstacao le) throws SQLException {
        String sql = "UPDATE linha_estacao SET id_linha = ?, id_estacao = ?, ordem = ?, sentido = ? WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, le.getIdLinha());
            stmt.setInt(2, le.getIdEstacao());
            stmt.setInt(3, le.getOrdem());
            stmt.setString(4, le.getSentido());
            stmt.setInt(5, le.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM linha_estacao WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void excluirPorLinha(int idLinha) throws SQLException {
        String sql = "DELETE FROM linha_estacao WHERE id_linha = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, idLinha);
            stmt.executeUpdate();
        }
    }

    private LinhaEstacao map(ResultSet rs) throws SQLException {
        LinhaEstacao le = new LinhaEstacao();
        le.setId(rs.getInt("id"));
        le.setIdLinha(rs.getInt("id_linha"));
        le.setIdEstacao(rs.getInt("id_estacao"));
        le.setOrdem(rs.getInt("ordem"));
        le.setSentido(rs.getString("sentido"));
        return le;
    }
}
