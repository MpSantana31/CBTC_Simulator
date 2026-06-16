package org.example.cbtc_simulator.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DAO<T> {
    void inserir(T entidade) throws SQLException;
    Optional<T> buscarPorId(int id) throws SQLException;
    List<T> listarTodos() throws SQLException;
    void atualizar(T entidade) throws SQLException;
    void excluir(int id) throws SQLException;
}
