package org.example.cbtc_simulator.service;

import org.example.cbtc_simulator.dao.TrechoDAO;
import org.example.cbtc_simulator.dao.TremDAO;
import org.example.cbtc_simulator.dao.ViagemProgramadaDAO;
import org.example.cbtc_simulator.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SimuladorCBTCService {

    private final TrechoDAO trechoDAO;
    private final TremDAO tremDAO;
    private final ViagemProgramadaDAO viagemDAO;

    private double tempoSimuladoSegundos;
    private final List<TremEmOperacao> trensAtivos;
    private final List<EventoSimulacao> eventos;
    private List<ViagemProgramada> viagensPendentes;

    public SimuladorCBTCService(TrechoDAO trechoDAO, TremDAO tremDAO, ViagemProgramadaDAO viagemDAO) {
        this.trechoDAO = trechoDAO;
        this.tremDAO = tremDAO;
        this.viagemDAO = viagemDAO;
        this.trensAtivos = new ArrayList<>();
        this.eventos = new ArrayList<>();
    }

    public void iniciarSimulacao() throws SQLException {
        viagemDAO.resetarTodas();
        tempoSimuladoSegundos = 0;
        trensAtivos.clear();
        eventos.clear();
        viagensPendentes = new ArrayList<>(viagemDAO.listarPendentes());
    }

    public void executarTick() throws SQLException {
        tempoSimuladoSegundos += CBTCConfig.TICK_SEGUNDOS;
        ativarViagens();

        for (TremEmOperacao trem : trensAtivos) {
            processarTickTrem(trem);
        }
    }

    private void ativarViagens() throws SQLException {
        if (viagensPendentes == null) return;

        Iterator<ViagemProgramada> it = viagensPendentes.iterator();
        while (it.hasNext()) {
            ViagemProgramada viagem = it.next();
            if (horarioParaSegundos(viagem.getHorarioPartida()) <= tempoSimuladoSegundos) {
                viagem.setRealizada(true);
                viagemDAO.atualizar(viagem);
                TremEmOperacao op = new TremEmOperacao(
                        viagem.getIdTrem(),
                        viagem.getIdLinha(),
                        0,
                        viagem.isIdaVolta() ? "IDA" : "VOLTA"
                );
                tremDAO.buscarPorId(viagem.getIdTrem())
                        .ifPresent(t -> op.setVelocidadeMax(t.getVelocidadeMaxKmh()));
                trensAtivos.add(op);
                eventos.add(new EventoSimulacao(
                        getTickAtual(), viagem.getIdTrem(), TipoEvento.PARTIDA,
                        "Iniciou viagem no sentido " + op.getDirecao()
                ));
                it.remove();
            }
        }
    }

    private void processarTickTrem(TremEmOperacao trem) throws SQLException {
        if (trem.getContadorEmbarque() > 0) {
            trem.setContadorEmbarque(trem.getContadorEmbarque() - 1);
            if (trem.getContadorEmbarque() <= 0) {
                trem.setStatus(StatusTrem.ACELERANDO);
                eventos.add(new EventoSimulacao(
                        getTickAtual(), trem.getIdTrem(), TipoEvento.EMBARQUE,
                        "Embarque concluído, partindo"
                ));
            }
            return;
        }

        List<Trecho> trechos = trechoDAO.listarPorLinhaESentido(trem.getIdLinha(), trem.getDirecao());
        if (trem.getTrechoOrdemAtual() >= trechos.size()) return;

        atualizarPosicao(trem);
        double ma = calcularMA(trem);
        double velocidadeAlvo = calcularVelocidadeAlvo(trem, ma);
        verificarHeadway(trem);
        trem.setVelocidadeAtualKmh(calcularNovaVelocidade(trem, velocidadeAlvo));
    }

    private void atualizarPosicao(TremEmOperacao trem) throws SQLException {
        List<Trecho> trechos = trechoDAO.listarPorLinhaESentido(trem.getIdLinha(), trem.getDirecao());
        if (trem.getTrechoOrdemAtual() >= trechos.size()) return;

        Trecho trechoAtual = trechos.get(trem.getTrechoOrdemAtual());
        double deltaKm = (trem.getVelocidadeAtualKmh() / 3.6) * CBTCConfig.TICK_SEGUNDOS / 1000.0;
        trem.setProgressoKm(trem.getProgressoKm() + deltaKm);

        if (trem.getProgressoKm() >= trechoAtual.getDistanciaKm()) {
            trem.setProgressoKm(0);
            eventos.add(new EventoSimulacao(
                    getTickAtual(), trem.getIdTrem(), TipoEvento.CHEGADA,
                    "Chegou à estação, trecho " + (trem.getTrechoOrdemAtual() + 1)
            ));
            int proxOrdem = trem.getTrechoOrdemAtual() + 1;
            if (proxOrdem >= trechos.size()) {
                eventos.add(new EventoSimulacao(
                        getTickAtual(), trem.getIdTrem(), TipoEvento.FINAL,
                        "Percurso completo"
                ));
                trem.setTrechoOrdemAtual(proxOrdem);
                trem.setStatus(StatusTrem.PARADO);
            } else {
                trem.setTrechoOrdemAtual(proxOrdem);
                trem.setStatus(StatusTrem.PARADO);
                trem.setContadorEmbarque((int) (CBTCConfig.TEMPO_EMBARQUE_SEGUNDOS / CBTCConfig.TICK_SEGUNDOS));
            }
        } else {
            trem.setStatus(StatusTrem.VELOCIDADE_CRUZEIRO);
        }
    }

    private double calcularMA(TremEmOperacao trem) throws SQLException {
        List<Trecho> trechos = trechoDAO.listarPorLinhaESentido(trem.getIdLinha(), trem.getDirecao());
        if (trem.getTrechoOrdemAtual() >= trechos.size()) return 0;

        Trecho trechoAtual = trechos.get(trem.getTrechoOrdemAtual());
        double distRestante = trechoAtual.getDistanciaKm() - trem.getProgressoKm();

        Optional<TremEmOperacao> tremFrente = trensAtivos.stream()
                .filter(t -> t.getIdTrem() != trem.getIdTrem())
                .filter(t -> t.getDirecao().equals(trem.getDirecao()))
                .filter(t -> t.getTrechoOrdemAtual() > trem.getTrechoOrdemAtual()
                        || (t.getTrechoOrdemAtual() == trem.getTrechoOrdemAtual()
                                && t.getProgressoKm() > trem.getProgressoKm()))
                .min(Comparator.comparingInt(TremEmOperacao::getTrechoOrdemAtual)
                        .thenComparingDouble(TremEmOperacao::getProgressoKm));

        if (tremFrente.isPresent()) {
            TremEmOperacao tf = tremFrente.get();
            Trem dadosTrem = tremDAO.buscarPorId(tf.getIdTrem()).orElse(null);
            double comprimentoKm = dadosTrem != null ? dadosTrem.getComprimentoM() / 1000.0 : 0;

            if (tf.getTrechoOrdemAtual() == trem.getTrechoOrdemAtual()) {
                double distEntreTrens = tf.getProgressoKm() - trem.getProgressoKm();
                if (distEntreTrens <= 0) return 0;
                double ma = distEntreTrens - comprimentoKm - CBTCConfig.MARGEM_SEGURANCA_KM;
                double maAntigo = ma;
                ma = Math.max(0, ma);
                if (ma < maAntigo) {
                    eventos.add(new EventoSimulacao(
                            getTickAtual(), trem.getIdTrem(), TipoEvento.MA_REDUZIDO,
                            "MA reduzido: trem à frente no mesmo trecho"
                    ));
                }
                return ma;
            } else {
                double ma = distRestante + trechoAtual.getDistanciaKm();
                return Math.max(0, ma);
            }
        }

        return distRestante;
    }

    private double calcularVelocidadeAlvo(TremEmOperacao trem, double ma) throws SQLException {
        Optional<Trem> dados = tremDAO.buscarPorId(trem.getIdTrem());
        if (dados.isEmpty()) return 0;

        double limiteTrem = dados.get().getVelocidadeMaxKmh();
        double distMaximaKm = CBTCConfig.DISTANCIA_FRENAGEM_PROPORCAO;

        if (ma <= 0) {
            trem.setStatus(StatusTrem.ATP_ACIONADO);
            return 0;
        }

        if (ma <= CBTCConfig.MARGEM_SEGURANCA_KM) {
            if (trem.getStatus() != StatusTrem.ATP_ACIONADO) {
                trem.setStatus(StatusTrem.ATP_ACIONADO);
                eventos.add(new EventoSimulacao(
                        getTickAtual(), trem.getIdTrem(), TipoEvento.FREIO_EMERGENCIA,
                        "ATP acionado: MA mínimo"
                ));
            }
            return CBTCConfig.VELOCIDADE_MINIMA_KMH;
        }

        if (ma >= distMaximaKm) {
            return Math.min(limiteTrem, 80);
        }

        double proporcao = ma / distMaximaKm;
        double velocidade = Math.max(CBTCConfig.VELOCIDADE_MINIMA_KMH, limiteTrem * proporcao);
        if (velocidade < trem.getVelocidadeAtualKmh()) {
            trem.setStatus(StatusTrem.REDUZINDO);
        }
        return Math.min(velocidade, limiteTrem);
    }

    private void verificarHeadway(TremEmOperacao trem) {
        TremEmOperacao frente = trensAtivos.stream()
                .filter(t -> t.getIdTrem() != trem.getIdTrem())
                .filter(t -> t.getDirecao().equals(trem.getDirecao()))
                .filter(t -> t.getTrechoOrdemAtual() > trem.getTrechoOrdemAtual()
                        || (t.getTrechoOrdemAtual() == trem.getTrechoOrdemAtual()
                                && t.getProgressoKm() > trem.getProgressoKm()))
                .min(Comparator.<TremEmOperacao>comparingInt(TremEmOperacao::getTrechoOrdemAtual)
                        .thenComparingDouble(TremEmOperacao::getProgressoKm))
                .orElse(null);

        if (frente == null) return;

        double espacoKm = frente.getProgressoKm() - trem.getProgressoKm();
        if (frente.getTrechoOrdemAtual() > trem.getTrechoOrdemAtual()) {
            espacoKm += 2.0;
        }
        if (espacoKm <= 0) return;

        double velFrente = Math.max(20, frente.getVelocidadeAtualKmh());
        double velTras = Math.max(20, trem.getVelocidadeAtualKmh());
        double velocidadeRelativa = velTras - velFrente;
        if (velocidadeRelativa <= 0) return;

        double tempoEstimado = (espacoKm * 1000 * 3.6) / velocidadeRelativa;
        if (tempoEstimado < CBTCConfig.HEADWAY_MINIMO_SEGUNDOS) {
            double reducao = 1 - (tempoEstimado / CBTCConfig.HEADWAY_MINIMO_SEGUNDOS);
            double velOriginal = trem.getVelocidadeAtualKmh();
            double velReduzida = velOriginal * (1 - reducao * 0.5);
            if (velOriginal - velReduzida < 0.5) return;
            trem.setVelocidadeAtualKmh(velReduzida);
            trem.setStatus(StatusTrem.REDUZINDO);
            eventos.add(new EventoSimulacao(
                    getTickAtual(), trem.getIdTrem(), TipoEvento.HEADWAY_AJUSTADO,
                    "Headway ajustado: espaco=" + String.format("%.2f", espacoKm) + "km"
            ));
        }
    }

    private double calcularNovaVelocidade(TremEmOperacao trem, double alvoKmh) {
        double diferenca = alvoKmh - trem.getVelocidadeAtualKmh();
        if (Math.abs(diferenca) < 1) return alvoKmh;
        if (diferenca > 0) {
            trem.setStatus(StatusTrem.ACELERANDO);
        }
        return trem.getVelocidadeAtualKmh() + diferenca * 0.3;
    }

    public void resetTudo() {
        tempoSimuladoSegundos = 0;
        trensAtivos.clear();
        eventos.clear();
        viagensPendentes = null;
    }

    public void resetSoTrens() {
        trensAtivos.clear();
        tempoSimuladoSegundos = 0;
    }

    private int getTickAtual() {
        return (int) (tempoSimuladoSegundos / CBTCConfig.TICK_SEGUNDOS);
    }

    private double horarioParaSegundos(String horario) {
        if (horario == null || !horario.contains(":")) return 0;
        try {
            String[] partes = horario.split(":");
            int h = Integer.parseInt(partes[0].trim());
            int m = Integer.parseInt(partes[1].trim());
            return h * 3600.0 + m * 60.0;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public double getTempoSimuladoSegundos() { return tempoSimuladoSegundos; }
    public List<TremEmOperacao> getTrensAtivos() { return trensAtivos; }
    public List<EventoSimulacao> getEventos() { return eventos; }

    public double getMaxVelocidade(int idTrem) throws SQLException {
        return tremDAO.buscarPorId(idTrem)
                .map(Trem::getVelocidadeMaxKmh)
                .orElse(0.0);
    }

    public double getKmNaLinha(int idTrem) throws SQLException {
        TremEmOperacao trem = trensAtivos.stream()
                .filter(t -> t.getIdTrem() == idTrem)
                .findFirst().orElse(null);
        if (trem == null) return 0;

        List<Trecho> trechos = trechoDAO.listarPorLinhaESentido(trem.getIdLinha(), trem.getDirecao());
        if (trem.getTrechoOrdemAtual() >= trechos.size()) return 0;

        double kmAcumulado = 0;
        for (int i = 0; i < trem.getTrechoOrdemAtual(); i++) {
            kmAcumulado += trechos.get(i).getDistanciaKm();
        }
        kmAcumulado += trem.getProgressoKm();
        return kmAcumulado;
    }

    public double calcularMA(int idTrem) throws SQLException {
        TremEmOperacao trem = trensAtivos.stream()
                .filter(t -> t.getIdTrem() == idTrem)
                .findFirst().orElse(null);
        if (trem == null) return 0;
        return calcularMA(trem);
    }

    public double calcularHeadway(int idTrem) throws SQLException {
        TremEmOperacao trem = trensAtivos.stream()
                .filter(t -> t.getIdTrem() == idTrem)
                .findFirst().orElse(null);
        if (trem == null) return 0;

        TremEmOperacao frente = trensAtivos.stream()
                .filter(t -> t.getIdTrem() != trem.getIdTrem())
                .filter(t -> t.getDirecao().equals(trem.getDirecao()))
                .filter(t -> t.getTrechoOrdemAtual() > trem.getTrechoOrdemAtual()
                        || (t.getTrechoOrdemAtual() == trem.getTrechoOrdemAtual()
                                && t.getProgressoKm() > trem.getProgressoKm()))
                .findFirst().orElse(null);
        if (frente == null) return 0;

        double espacoKm = frente.getProgressoKm() - trem.getProgressoKm();
        if (frente.getTrechoOrdemAtual() > trem.getTrechoOrdemAtual()) {
            espacoKm += 2.0;
        }
        return Math.max(0, espacoKm);
    }

    public String getTremAtras(int idTrem) {
        TremEmOperacao trem = trensAtivos.stream()
                .filter(t -> t.getIdTrem() == idTrem)
                .findFirst().orElse(null);
        if (trem == null) return "-";
        return trensAtivos.stream()
                .filter(t -> t.getDirecao().equals(trem.getDirecao()))
                .filter(t -> t.getTrechoOrdemAtual() < trem.getTrechoOrdemAtual()
                        || (t.getTrechoOrdemAtual() == trem.getTrechoOrdemAtual()
                                && t.getProgressoKm() < trem.getProgressoKm()))
                .findFirst()
                .map(t -> "T" + t.getIdTrem())
                .orElse("-");
    }

    public String getTremFrente(int idTrem) {
        TremEmOperacao trem = trensAtivos.stream()
                .filter(t -> t.getIdTrem() == idTrem)
                .findFirst().orElse(null);
        if (trem == null) return "-";
        return trensAtivos.stream()
                .filter(t -> t.getIdTrem() != trem.getIdTrem())
                .filter(t -> t.getDirecao().equals(trem.getDirecao()))
                .filter(t -> t.getTrechoOrdemAtual() > trem.getTrechoOrdemAtual()
                        || (t.getTrechoOrdemAtual() == trem.getTrechoOrdemAtual()
                                && t.getProgressoKm() > trem.getProgressoKm()))
                .findFirst()
                .map(t -> "T" + t.getIdTrem())
                .orElse("-");
    }

    public String getETA(int idTrem) throws SQLException {
        TremEmOperacao trem = trensAtivos.stream()
                .filter(t -> t.getIdTrem() == idTrem)
                .findFirst().orElse(null);
        if (trem == null) return "-";

        List<Trecho> trechos = trechoDAO.listarPorLinhaESentido(trem.getIdLinha(), trem.getDirecao());
        if (trem.getTrechoOrdemAtual() >= trechos.size()) return "FIM";

        double distTotal = 0;
        for (int i = trem.getTrechoOrdemAtual(); i < trechos.size(); i++) {
            distTotal += trechos.get(i).getDistanciaKm();
            if (i == trem.getTrechoOrdemAtual()) {
                distTotal -= trem.getProgressoKm();
            }
        }
        double vel = Math.max(20, trem.getVelocidadeAtualKmh());
        double segundos = (distTotal * 3600) / vel;
        int min = (int) segundos / 60;
        int seg = (int) segundos % 60;
        return String.format("%02d:%02d", min, seg);
    }
}
