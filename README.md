# CBTC Simulator

Simulador de linhas de metrô com controle CBTC. Trabalho final de Linguagem de Programação 1.

## Como Rodar

```bash
./mvnw clean compile
./mvnw javafx:run
```

Ao iniciar, o app cria automaticamente o banco e popula com dados de exemplo (estações, linhas, trens e viagens).

## Configuração do Banco

A `ConnectionFactory` lê a configuração na seguinte ordem de prioridade:

1. **Variáveis de ambiente** (mais prático pra outros lugares):
   - `DB_URL` (ex: `jdbc:mysql://outro-host:3306/meu_banco`)
   - `DB_USER`
   - `DB_PASSWORD`

2. **Arquivo `src/main/resources/db.properties`** (substitua o existente):
   ```properties
   db.url=jdbc:mysql://localhost:3306/cbtc_simulator
   db.user=root
   db.password=
   ```

3. **Valores padrão** (já embutidos no código, funcionam local sem config).

Pra usar em outro lugar, basta setar as variáveis de ambiente antes de rodar:
```bash
DB_URL=jdbc:mysql://meu-servidor:3306/cbtc DB_USER=meu_user DB_PASSWORD=minha_senha ./mvnw javafx:run
```

## Schema

```bash
mysql -u root < src/main/resources/sql/init.sql
```

## Testes

```bash
./mvnw test
```

## Conceitos

- **CBTC** — Communication-Based Train Control
- **Tick discreto** — cada tick = 10s simulados
- **MA** (Movement Authority) — distância segura até obstáculo
- **Headway** — intervalo mínimo entre trens (90s)
- **ATP** (Automatic Train Protection) — freio automático
- **Via dupla** — IDA e VOLTA em trilhos separados

## Stack

- Java 17 + JavaFX 21
- MySQL 8
- Maven
- JUnit 5 + Mockito

## Estrutura

```
src/main/java/org/example/cbtc_simulator/
├── Main, MainApp, SeedData
├── exception/   exceções customizadas
├── model/       entidades + enums
├── dao/         DAOs + ConnectionFactory
├── service/     SimuladorCBTCService + CBTCConfig
└── view/        Controllers + FXMLs + LinhaVisualPane
```

## Comandos Úteis

```bash
# Simulação via terminal (sem GUI), 50 ticks
./mvnw exec:java -Dexec.mainClass="org.example.cbtc_simulator.SimuladorRun" -Dexec.args="50"

# Recriar schema e seed
mysql -u root -e "DROP DATABASE IF EXISTS cbtc_simulator; CREATE DATABASE cbtc_simulator DEFAULT CHARACTER SET utf8mb4;"
mysql -u root cbtc_simulator < src/main/resources/sql/init.sql
./mvnw exec:java -Dexec.mainClass="org.example.cbtc_simulator.SeedData"
```
