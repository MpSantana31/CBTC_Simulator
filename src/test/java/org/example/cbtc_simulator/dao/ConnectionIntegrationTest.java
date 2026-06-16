package org.example.cbtc_simulator.dao;

import org.example.cbtc_simulator.model.Estacao;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionIntegrationTest {

    @Test
    void conexaoFunciona() {
        try (Connection c = ConnectionFactory.getConnection()) {
            assertNotNull(c);
            assertFalse(c.isClosed());
            System.out.println("Conectado ao MySQL: " + c.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            fail("Falha na conexão: " + e.getMessage());
        }
    }

    @Test
    void crudEstacao() throws Exception {
        EstacaoDAO dao = new EstacaoDAO();

        Estacao e = new Estacao("EstaÃ§Ã£o Teste", "Zona Sul");
        dao.inserir(e);
        assertNotNull(e.getId());

        Optional<Estacao> buscada = dao.buscarPorId(e.getId());
        assertTrue(buscada.isPresent());
        assertEquals("EstaÃ§Ã£o Teste", buscada.get().getNome());

        buscada.get().setNome("EstaÃ§Ã£o Modificada");
        dao.atualizar(buscada.get());

        Optional<Estacao> modificada = dao.buscarPorId(e.getId());
        assertTrue(modificada.isPresent());
        assertEquals("EstaÃ§Ã£o Modificada", modificada.get().getNome());

        dao.excluir(e.getId());
        assertTrue(dao.buscarPorId(e.getId()).isEmpty());

        System.out.println("CRUD Estacao funcionou!");
    }
}
