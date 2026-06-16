package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.Estacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstacaoDAO implements DAO<Estacao> {
    @Override
    public void inserir(Estacao e) throws SQLException {
        String sql = "INSERT INTO estacao (nome, zona) VALUES (?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, e.getNome());
            stmt.setString(2, e.getZona());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Optional<Estacao> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM estacao WHERE id = ?";
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
    public List<Estacao> listarTodos() throws SQLException {
        List<Estacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM estacao ORDER BY nome";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    @Override
    public void atualizar(Estacao e) throws SQLException {
        String sql = "UPDATE estacao SET nome = ?, zona = ? WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, e.getNome());
            stmt.setString(2, e.getZona());
            stmt.setInt(3, e.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM estacao WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Estacao map(ResultSet rs) throws SQLException {
        Estacao e = new Estacao();
        e.setId(rs.getInt("id"));
        e.setNome(rs.getString("nome"));
        e.setZona(rs.getString("zona"));
        return e;
    }
}
