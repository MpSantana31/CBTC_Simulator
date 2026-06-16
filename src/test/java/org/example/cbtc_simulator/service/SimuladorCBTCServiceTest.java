package org.example.cbtc_simulator.service;

import org.example.cbtc_simulator.dao.TrechoDAO;
import org.example.cbtc_simulator.dao.TremDAO;
import org.example.cbtc_simulator.dao.ViagemProgramadaDAO;
import org.example.cbtc_simulator.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimuladorCBTCServiceTest {

    @Mock private TrechoDAO trechoDAO;
    @Mock private TremDAO tremDAO;
    @Mock private ViagemProgramadaDAO viagemDAO;

    private SimuladorCBTCService simulador;

    private final Trecho trechoUnico = new Trecho(1, "IDA", 1, 2, 0.5, 0);
    private final Trem tremTeste = new Trem("Teste", 60, 1.0, 200, 40);

    @BeforeEach
    void setup() throws SQLException {
        simulador = new SimuladorCBTCService(trechoDAO, tremDAO, viagemDAO);
    }

    @Test
    void tickAvançaTrem() throws SQLException {
        when(viagemDAO.listarPendentes()).thenReturn(List.of(
                new ViagemProgramada(1, 1, "00:00", true)
        ));
        when(trechoDAO.listarPorLinhaESentido(anyInt(), eq("IDA"))).thenReturn(List.of(trechoUnico));
        when(tremDAO.buscarPorId(1)).thenReturn(Optional.of(tremTeste));

        simulador.iniciarSimulacao();
        simulador.executarTick();

        assertFalse(simulador.getTrensAtivos().isEmpty());
        assertTrue(simulador.getTempoSimuladoSegundos() > 0);
    }

    @Test
    void maReduzComTremAFrente() throws SQLException {
        when(viagemDAO.listarPendentes()).thenReturn(List.of(
                new ViagemProgramada(1, 1, "00:00", true),
                new ViagemProgramada(2, 1, "00:00", true)
        ));
        when(trechoDAO.listarPorLinhaESentido(anyInt(), eq("IDA"))).thenReturn(List.of(trechoUnico));
        when(tremDAO.buscarPorId(1)).thenReturn(Optional.of(tremTeste));
        when(tremDAO.buscarPorId(2)).thenReturn(Optional.of(tremTeste));

        simulador.iniciarSimulacao();
        simulador.executarTick();

        TremEmOperacao t1 = simulador.getTrensAtivos().get(0);
        TremEmOperacao t2 = simulador.getTrensAtivos().get(1);

        assertEquals(t1.getDirecao(), t2.getDirecao());
    }

    @Test
    void paradaEmEstacao() throws SQLException {
        when(viagemDAO.listarPendentes()).thenReturn(List.of(
                new ViagemProgramada(1, 1, "00:00", true)
        ));
        when(trechoDAO.listarPorLinhaESentido(anyInt(), eq("IDA"))).thenReturn(List.of(
                new Trecho(1, "IDA", 1, 2, 0.5, 0),
                new Trecho(1, "IDA", 2, 3, 0.5, 1)
        ));
        when(tremDAO.buscarPorId(1)).thenReturn(Optional.of(tremTeste));

        simulador.iniciarSimulacao();
        for (int i = 0; i < 30; i++) {
            simulador.executarTick();
        }

        long chegadas = simulador.getEventos().stream()
                .filter(e -> e.getTipoEvento() == TipoEvento.CHEGADA)
                .count();
        assertTrue(chegadas >= 1, "Esperava ao menos 1 CHEGADA, mas teve " + chegadas);
    }

    @Test
    void tremCompletaPercurso() throws SQLException {
        when(viagemDAO.listarPendentes()).thenReturn(List.of(
                new ViagemProgramada(1, 1, "00:00", true)
        ));
        when(trechoDAO.listarPorLinhaESentido(anyInt(), eq("IDA"))).thenReturn(List.of(
                new Trecho(1, "IDA", 1, 2, 0.5, 0),
                new Trecho(1, "IDA", 2, 3, 0.5, 1)
        ));
        when(tremDAO.buscarPorId(1)).thenReturn(Optional.of(tremTeste));

        simulador.iniciarSimulacao();
        for (int i = 0; i < 30; i++) {
            simulador.executarTick();
        }

        boolean finalRegistrado = simulador.getEventos().stream()
                .anyMatch(e -> e.getTipoEvento() == TipoEvento.FINAL);
        assertTrue(finalRegistrado, "Esperava evento FINAL");
    }
}
