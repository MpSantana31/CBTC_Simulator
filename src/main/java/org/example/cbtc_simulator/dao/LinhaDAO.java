package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.Linha;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinhaDAO implements DAO<Linha> {
    @Override
    public void inserir(Linha l) throws SQLException {
        String sql = "INSERT INTO linha (nome, cor_hex, velocidade_max_kmh, tipo_via) VALUES (?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, l.getNome());
            stmt.setString(2, l.getCorHex());
            stmt.setDouble(3, l.getVelocidadeMaxKmh());
            stmt.setString(4, l.getTipoVia());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) l.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Optional<Linha> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM linha WHERE id = ?";
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
    public List<Linha> listarTodos() throws SQLException {
        List<Linha> lista = new ArrayList<>();
        String sql = "SELECT * FROM linha ORDER BY nome";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    @Override
    public void atualizar(Linha l) throws SQLException {
        String sql = "UPDATE linha SET nome = ?, cor_hex = ?, velocidade_max_kmh = ?, tipo_via = ? WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, l.getNome());
            stmt.setString(2, l.getCorHex());
            stmt.setDouble(3, l.getVelocidadeMaxKmh());
            stmt.setString(4, l.getTipoVia());
            stmt.setInt(5, l.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM linha WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Linha map(ResultSet rs) throws SQLException {
        Linha l = new Linha();
        l.setId(rs.getInt("id"));
        l.setNome(rs.getString("nome"));
        l.setCorHex(rs.getString("cor_hex"));
        l.setVelocidadeMaxKmh(rs.getDouble("velocidade_max_kmh"));
        l.setTipoVia(rs.getString("tipo_via"));
        return l;
    }
}
