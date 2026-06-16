package org.example.cbtc_simulator;

import org.example.cbtc_simulator.dao.*;
import org.example.cbtc_simulator.service.SimuladorCBTCService;

import java.sql.SQLException;

public class SimuladorRun {

    public static void main(String[] args) throws SQLException {
        int nTicks = args.length > 0 ? Integer.parseInt(args[0]) : 50;

        TrechoDAO trechoDAO = new TrechoDAO();
        TremDAO tremDAO = new TremDAO();
        ViagemProgramadaDAO viagemDAO = new ViagemProgramadaDAO();

        SimuladorCBTCService sim = new SimuladorCBTCService(trechoDAO, tremDAO, viagemDAO);
        sim.iniciarSimulacao();

        System.out.println("=== INICIO DA SIMULACAO ===");
        for (int i = 1; i <= nTicks; i++) {
            sim.executarTick();
            int ativos = sim.getTrensAtivos().size();
            StringBuilder status = new StringBuilder();
            for (var t : sim.getTrensAtivos()) {
                status.append(String.format("  Trem%d: trecho=%d prog=%.2fkm vel=%.1fkm/h status=%s | ",
                        t.getIdTrem(), t.getTrechoOrdemAtual(), t.getProgressoKm(),
                        t.getVelocidadeAtualKmh(), t.getStatus()));
            }
            System.out.printf("[Tick %2d] ativos=%d | %s%n", i, ativos, status);
        }
        System.out.println("=== EVENTOS ===");
        for (var e : sim.getEventos()) {
            System.out.printf("  [T%d] Trem%d %s: %s%n",
                    e.getTick(), e.getIdTrem(), e.getTipoEvento(), e.getMensagem());
        }
    }
}
