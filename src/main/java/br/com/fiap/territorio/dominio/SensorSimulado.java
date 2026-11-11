package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.FonteIndisponivelException;

import java.util.Random;

/**
 * Base comum dos sensores simulados. Usa um "passeio aleatório": cada nova
 * leitura parte da anterior e varia um pouco, o que gera tendências
 * (chuva aumentando ou diminuindo) em vez de números soltos.
 *
 * Também simula o mundo real: às vezes o sensor cai (FonteIndisponivelException)
 * e às vezes devolve lixo (valor negativo), que a Medicao vai rejeitar.
 */
public abstract class SensorSimulado implements Sensor {

    private static final double CHANCE_FALHA = 0.05;     // 5% das leituras: sensor não responde
    private static final double CHANCE_LEITURA_RUIM = 0.03; // 3%: devolve valor impossível

    private final String id;
    private final Random random;
    private boolean ativo = true;
    private double ultimoValor;

    protected SensorSimulado(String id, double valorInicial, Random random) {
        this.id = id;
        this.ultimoValor = valorInicial;
        this.random = random;
    }

    /** Cada sensor define o quanto o valor pode variar entre uma leitura e outra. */
    protected abstract double variacaoMaxima();

    /** Faixa "realista" do passeio aleatório (menor que a faixa física). */
    protected abstract double minimoRealista();
    protected abstract double maximoRealista();

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!ativo) {
            throw new FonteIndisponivelException(
                    "Sensor " + id + " (" + getTipo().getDescricao() + ") está desativado para manutenção.");
        }
        if (random.nextDouble() < CHANCE_FALHA) {
            throw new FonteIndisponivelException(
                    "Sensor " + id + " (" + getTipo().getDescricao() + ") não respondeu: sem sinal da estação.");
        }
        if (random.nextDouble() < CHANCE_LEITURA_RUIM) {
            return -1.0; // leitura corrompida, será barrada pela validação da Medicao
        }
        double variacao = (random.nextDouble() * 2 - 1) * variacaoMaxima();
        ultimoValor = limitar(ultimoValor + variacao);
        return arredondar(ultimoValor);
    }

    private double limitar(double valor) {
        return Math.max(minimoRealista(), Math.min(maximoRealista(), valor));
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public boolean estaFuncionando() {
        return ativo;
    }

    @Override
    public void ativar() { this.ativo = true; }

    @Override
    public void desativar() { this.ativo = false; }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id + " - " + getTipo().getDescricao() + " (" + getTipo().getUnidade() + ")"
                + (ativo ? "" : " [DESATIVADO]");
    }
}
