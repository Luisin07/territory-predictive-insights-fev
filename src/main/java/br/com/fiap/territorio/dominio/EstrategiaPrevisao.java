package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.util.List;

/**
 * Molde de qualquer forma de prever o próximo valor de um sensor.
 * O serviço chama prever() sem saber qual estratégia concreta está usando:
 * isso é polimorfismo.
 */
public abstract class EstrategiaPrevisao {

    private final String nome;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(String nome, int minimoLeituras) {
        if (minimoLeituras < 1) {
            throw new DadosInvalidosException("Uma estratégia precisa de pelo menos 1 leitura, recebeu " + minimoLeituras);
        }
        this.nome = nome;
        this.minimoLeituras = minimoLeituras;
    }

    /**
     * Prevê o próximo valor a partir do histórico.
     * @param historico medições de UM sensor em ordem cronológica (mais antiga primeiro)
     */
    public abstract Previsao prever(String sensorId, TipoSensor tipo, List<Medicao> historico);

    /** Regra comum a todas as estratégias: histórico nulo é erro de programação, curto é "não sei". */
    protected boolean temLeiturasSuficientes(List<Medicao> historico) {
        if (historico == null) {
            throw new DadosInvalidosException("Histórico nulo enviado para a " + nome + ".");
        }
        return historico.size() >= minimoLeituras;
    }

    protected double media(List<Medicao> medicoes) {
        return medicoes.stream().mapToDouble(Medicao::getValor).average().orElse(0.0);
    }

    protected double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /** Descrição base; as filhas complementam com super.descrever(). */
    public String descrever() {
        return nome + " (mínimo de " + minimoLeituras + " leituras)";
    }

    public String getNome() { return nome; }
    public int getMinimoLeituras() { return minimoLeituras; }
}
