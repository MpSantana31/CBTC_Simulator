package org.example.cbtc_simulator.service;

public class CBTCConfig {
    public static final double TICK_SEGUNDOS = 10.0;
    public static final double HEADWAY_MINIMO_SEGUNDOS = 90.0;
    public static final double MARGEM_SEGURANCA_KM = 0.05;
    public static final double TEMPO_EMBARQUE_SEGUNDOS = 20.0;
    public static final double VELOCIDADE_MINIMA_KMH = 5.0;
    public static final double DISTANCIA_FRENAGEM_PROPORCAO = 0.3;

    private CBTCConfig() {}
}
