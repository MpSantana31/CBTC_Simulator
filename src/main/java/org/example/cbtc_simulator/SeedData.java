package org.example.cbtc_simulator;

import org.example.cbtc_simulator.dao.*;
import org.example.cbtc_simulator.model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeedData {

    public static void main(String[] args) {
        seedCompleto();
        resetarViagens();
    }

    public static void seedCompleto() {
        try {
            EstacaoDAO estDAO = new EstacaoDAO();
            LinhaDAO linDAO = new LinhaDAO();
            LinhaEstacaoDAO leDAO = new LinhaEstacaoDAO();
            TrechoDAO trDAO = new TrechoDAO();
            TremDAO tremDAO = new TremDAO();
            ViagemProgramadaDAO viaDAO = new ViagemProgramadaDAO();

            if (!estDAO.listarTodos().isEmpty()) {
                System.out.println("Banco ja populado. Pulando seed.");
                return;
            }

            String[] nomesEstacoes = {
                    "Luz", "Se", "Republica", "Santa Cecilia",
                    "Marechal Deodoro", "Palmeiras-Barra Funda",
                    "Tiete", "Carandiru"
            };
            List<Estacao> estacoes = new ArrayList<>();
            for (String nome : nomesEstacoes) {
                Estacao e = new Estacao(nome, "Centro");
                estDAO.inserir(e);
                estacoes.add(e);
            }

            Linha linha1 = new Linha("Linha Vermelha", "#E74C3C", 80, "SUBTERRANEA");
            linDAO.inserir(linha1);
            Linha linha2 = new Linha("Linha Azul", "#3498DB", 70, "SUBTERRANEA");
            linDAO.inserir(linha2);

            for (Linha linha : new Linha[]{linha1, linha2}) {
                for (String sentido : new String[]{"IDA", "VOLTA"}) {
                    for (int i = 0; i < estacoes.size(); i++) {
                        leDAO.inserir(new LinhaEstacao(linha.getId(), estacoes.get(i).getId(), i, sentido));
                    }
                }
            }

            double[] dists = {1.8, 1.5, 2.0, 1.6, 2.1, 1.7, 1.4};
            for (int i = 0; i < estacoes.size() - 1; i++) {
                trDAO.inserir(new Trecho(linha1.getId(), "IDA",
                        estacoes.get(i).getId(), estacoes.get(i + 1).getId(), dists[i], i));
                trDAO.inserir(new Trecho(linha1.getId(), "VOLTA",
                        estacoes.get(estacoes.size() - 1 - i).getId(), estacoes.get(estacoes.size() - 2 - i).getId(), dists[i], i));
            }

            double[] dists2 = {1.6, 1.8, 1.4, 1.9, 1.7, 1.5, 1.6};
            for (int i = 0; i < estacoes.size() - 1; i++) {
                trDAO.inserir(new Trecho(linha2.getId(), "IDA",
                        estacoes.get(i).getId(), estacoes.get(i + 1).getId(), dists2[i], i));
                trDAO.inserir(new Trecho(linha2.getId(), "VOLTA",
                        estacoes.get(estacoes.size() - 1 - i).getId(), estacoes.get(estacoes.size() - 2 - i).getId(), dists2[i], i));
            }

            String[] modelos = {"Frota A", "Frota B", "Frota C", "Frota D"};
            double[] vels = {70, 65, 60, 75};
            for (int i = 0; i < modelos.length; i++) {
                Trem trem = new Trem(modelos[i], vels[i], 1.0, 200 + i * 20, 35 + i * 5);
                tremDAO.inserir(trem);
            }

            String[] horarios = {"00:00", "00:01", "00:02", "00:03", "00:04", "00:05"};
            boolean[] naLinha1 = {true, true, true, false, false, false};
            boolean[] sentidoIda = {true, true, false, true, false, true};
            int[] tremIds = {1, 2, 3, 4, 1, 2};
            for (int i = 0; i < horarios.length; i++) {
                int linhaId = naLinha1[i] ? linha1.getId() : linha2.getId();
                viaDAO.inserir(new ViagemProgramada(tremIds[i], linhaId, horarios[i], sentidoIda[i]));
            }

            int totalTrechos = (estacoes.size() - 1) * 2 * 2;
            System.out.println("Seed data criado: " + estacoes.size() + " estacoes, 2 linhas, "
                    + totalTrechos + " trechos, 4 trens, " + horarios.length + " viagens");

        } catch (SQLException e) {
            System.err.println("Erro ao criar seed: " + e.getMessage());
        }
    }

    public static void resetarViagens() {
        try {
            new ViagemProgramadaDAO().resetarTodas();
        } catch (SQLException e) {
            System.err.println("Erro ao resetar viagens: " + e.getMessage());
        }
    }
}
