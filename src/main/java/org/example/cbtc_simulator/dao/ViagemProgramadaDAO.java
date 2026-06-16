package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.ViagemProgramada;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViagemProgramadaDAO implements DAO<ViagemProgramada> {
    @Override
    public void inserir(ViagemProgramada v) throws SQLException {
        String sql = "INSERT INTO viagem_programada (id_trem, id_linha, horario_partida, ida_volta, realizada) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, v.getIdTrem());
            stmt.setInt(2, v.getIdLinha());
            stmt.setString(3, v.getHorarioPartida());
            stmt.setBoolean(4, v.isIdaVolta());
            stmt.setBoolean(5, v.isRealizada());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) v.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Optional<ViagemProgramada> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM viagem_programada WHERE id = ?";
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
    public List<ViagemProgramada> listarTodos() throws SQLException {
        List<ViagemProgramada> lista = new ArrayList<>();
        String sql = "SELECT * FROM viagem_programada ORDER BY horario_partida";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<ViagemProgramada> listarPendentes() throws SQLException {
        List<ViagemProgramada> lista = new ArrayList<>();
        String sql = "SELECT * FROM viagem_programada WHERE realizada = FALSE ORDER BY horario_partida";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    @Override
    public void atualizar(ViagemProgramada v) throws SQLException {
        String sql = "UPDATE viagem_programada SET id_trem = ?, id_linha = ?, horario_partida = ?, ida_volta = ?, realizada = ? WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, v.getIdTrem());
            stmt.setInt(2, v.getIdLinha());
            stmt.setString(3, v.getHorarioPartida());
            stmt.setBoolean(4, v.isIdaVolta());
            stmt.setBoolean(5, v.isRealizada());
            stmt.setInt(6, v.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM viagem_programada WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private ViagemProgramada map(ResultSet rs) throws SQLException {
        ViagemProgramada v = new ViagemProgramada();
        v.setId(rs.getInt("id"));
        v.setIdTrem(rs.getInt("id_trem"));
        v.setIdLinha(rs.getInt("id_linha"));
        v.setHorarioPartida(rs.getString("horario_partida"));
        v.setIdaVolta(rs.getBoolean("ida_volta"));
        v.setRealizada(rs.getBoolean("realizada"));
        return v;
    }

    public void resetarTodas() throws SQLException {
        String sql = "UPDATE viagem_programada SET realizada = FALSE";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}
