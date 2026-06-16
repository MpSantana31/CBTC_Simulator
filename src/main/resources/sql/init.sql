CREATE DATABASE IF NOT EXISTS cbtc_simulator
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE cbtc_simulator;

CREATE TABLE estacao (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE,
    zona VARCHAR(50)
);

CREATE TABLE linha (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cor_hex VARCHAR(7) NOT NULL,
    velocidade_max_kmh DOUBLE NOT NULL,
    tipo_via VARCHAR(20) CHECK (tipo_via IN ('SUPERFICIE','ELEVADA','SUBTERRANEA'))
);

CREATE TABLE linha_estacao (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_linha INTEGER NOT NULL,
    id_estacao INTEGER NOT NULL,
    ordem INTEGER NOT NULL,
    sentido VARCHAR(5) NOT NULL CHECK (sentido IN ('IDA','VOLTA')),
    UNIQUE (id_linha, id_estacao, sentido),
    FOREIGN KEY (id_linha) REFERENCES linha(id),
    FOREIGN KEY (id_estacao) REFERENCES estacao(id)
);

CREATE TABLE trecho (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_linha INTEGER NOT NULL,
    sentido VARCHAR(5) NOT NULL CHECK (sentido IN ('IDA','VOLTA')),
    id_estacao_origem INTEGER NOT NULL,
    id_estacao_destino INTEGER NOT NULL,
    CHECK (id_estacao_origem != id_estacao_destino),
    distancia_km DOUBLE NOT NULL CHECK (distancia_km > 0),
    ordem INTEGER NOT NULL,
    UNIQUE (id_linha, sentido, ordem),
    FOREIGN KEY (id_linha) REFERENCES linha(id),
    FOREIGN KEY (id_estacao_origem) REFERENCES estacao(id),
    FOREIGN KEY (id_estacao_destino) REFERENCES estacao(id)
);

CREATE TABLE trem (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    modelo VARCHAR(100) NOT NULL,
    velocidade_max_kmh DOUBLE NOT NULL,
    aceleracao_ms2 DOUBLE NOT NULL,
    capacidade INTEGER NOT NULL,
    comprimento_m DOUBLE NOT NULL DEFAULT 40
);

CREATE TABLE viagem_programada (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_trem INTEGER NOT NULL,
    id_linha INTEGER NOT NULL,
    horario_partida VARCHAR(5) NOT NULL,
    ida_volta BOOLEAN NOT NULL DEFAULT TRUE,
    realizada BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_trem) REFERENCES trem(id),
    FOREIGN KEY (id_linha) REFERENCES linha(id)
);
