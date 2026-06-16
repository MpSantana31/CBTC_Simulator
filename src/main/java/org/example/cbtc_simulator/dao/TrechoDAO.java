package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.Trecho;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrechoDAO implements DAO<Trecho> {
    @Override
    public void inserir(Trecho t) throws SQLException {
        String sql = "INSERT INTO trecho (id_linha, sentido, id_estacao_origem, id_estacao_destino, distancia_km, ordem) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, t.getIdLinha());
            stmt.setString(2, t.getSentido());
            stmt.setInt(3, t.getIdEstacaoOrigem());
            stmt.setInt(4, t.getIdEstacaoDestino());
            stmt.setDouble(5, t.getDistanciaKm());
            stmt.setInt(6, t.getOrdem());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Optional<Trecho> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM trecho WHERE id = ?";
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
    public List<Trecho> listarTodos() throws SQLException {
        List<Trecho> lista = new ArrayList<>();
        String sql = "SELECT * FROM trecho ORDER BY id_linha, sentido, ordem";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<Trecho> listarPorLinhaESentido(int idLinha, String sentido) throws SQLException {
        List<Trecho> lista = new ArrayList<>();
        String sql = "SELECT * FROM trecho WHERE id_linha = ? AND sentido = ? ORDER BY ordem";
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
    public void atualizar(Trecho t) throws SQLException {
        String sql = "UPDATE trecho SET id_linha = ?, sentido = ?, id_estacao_origem = ?, id_estacao_destino = ?, distancia_km = ?, ordem = ? WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, t.getIdLinha());
            stmt.setString(2, t.getSentido());
            stmt.setInt(3, t.getIdEstacaoOrigem());
            stmt.setInt(4, t.getIdEstacaoDestino());
            stmt.setDouble(5, t.getDistanciaKm());
            stmt.setInt(6, t.getOrdem());
            stmt.setInt(7, t.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM trecho WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Trecho map(ResultSet rs) throws SQLException {
        Trecho t = new Trecho();
        t.setId(rs.getInt("id"));
        t.setIdLinha(rs.getInt("id_linha"));
        t.setSentido(rs.getString("sentido"));
        t.setIdEstacaoOrigem(rs.getInt("id_estacao_origem"));
        t.setIdEstacaoDestino(rs.getInt("id_estacao_destino"));
        t.setDistanciaKm(rs.getDouble("distancia_km"));
        t.setOrdem(rs.getInt("ordem"));
        return t;
    }
}
