package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.Trem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TremDAO implements DAO<Trem> {
    @Override
    public void inserir(Trem t) throws SQLException {
        String sql = "INSERT INTO trem (modelo, velocidade_max_kmh, aceleracao_ms2, capacidade, comprimento_m) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, t.getModelo());
            stmt.setDouble(2, t.getVelocidadeMaxKmh());
            stmt.setDouble(3, t.getAceleracaoMs2());
            stmt.setInt(4, t.getCapacidade());
            stmt.setDouble(5, t.getComprimentoM());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Optional<Trem> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM trem WHERE id = ?";
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
    public List<Trem> listarTodos() throws SQLException {
        List<Trem> lista = new ArrayList<>();
        String sql = "SELECT * FROM trem ORDER BY modelo";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    @Override
    public void atualizar(Trem t) throws SQLException {
        String sql = "UPDATE trem SET modelo = ?, velocidade_max_kmh = ?, aceleracao_ms2 = ?, capacidade = ?, comprimento_m = ? WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, t.getModelo());
            stmt.setDouble(2, t.getVelocidadeMaxKmh());
            stmt.setDouble(3, t.getAceleracaoMs2());
            stmt.setInt(4, t.getCapacidade());
            stmt.setDouble(5, t.getComprimentoM());
            stmt.setInt(6, t.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM trem WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Trem map(ResultSet rs) throws SQLException {
        Trem t = new Trem();
        t.setId(rs.getInt("id"));
        t.setModelo(rs.getString("modelo"));
        t.setVelocidadeMaxKmh(rs.getDouble("velocidade_max_kmh"));
        t.setAceleracaoMs2(rs.getDouble("aceleracao_ms2"));
        t.setCapacidade(rs.getInt("capacidade"));
        t.setComprimentoM(rs.getDouble("comprimento_m"));
        return t;
    }
}
