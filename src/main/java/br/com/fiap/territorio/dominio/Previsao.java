package br.com.fiap.territorio.dominio;

/**
 * Resultado de uma estratégia de previsão: o valor previsto e o que ele significa.
 * Se não houver dados suficientes, a previsão é marcada como NÃO confiável
 * e o sistema não emite alerta com base nela ("o sistema se cala").
 */
public class Previsao {

    private final String sensorId;
    private final TipoSensor tipo;
    private final String estrategia;
    private final double valorPrevisto;
    private final int leiturasUsadas;
    private final boolean confiavel;
    private final String interpretacao;

    private Previsao(String sensorId, TipoSensor tipo, String estrategia, double valorPrevisto,
                     int leiturasUsadas, boolean confiavel, String interpretacao) {
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.estrategia = estrategia;
        this.valorPrevisto = valorPrevisto;
        this.leiturasUsadas = leiturasUsadas;
        this.confiavel = confiavel;
        this.interpretacao = interpretacao;
    }

    public static Previsao confiavel(String sensorId, TipoSensor tipo, String estrategia,
                                     double valorPrevisto, int leiturasUsadas, String interpretacao) {
        return new Previsao(sensorId, tipo, estrategia, valorPrevisto, leiturasUsadas, true, interpretacao);
    }

    public static Previsao insuficiente(String sensorId, TipoSensor tipo, String estrategia,
                                        int leiturasDisponiveis, int leiturasNecessarias) {
        String motivo = String.format(
                "Sem previsão: só há %d leitura(s) no histórico e a %s precisa de pelo menos %d. "
                        + "Melhor ficar em silêncio do que alertar com base em chute.",
                leiturasDisponiveis, estrategia, leiturasNecessarias);
        return new Previsao(sensorId, tipo, estrategia, Double.NaN, leiturasDisponiveis, false, motivo);
    }

    public String getSensorId() { return sensorId; }
    public TipoSensor getTipo() { return tipo; }
    public String getEstrategia() { return estrategia; }
    public double getValorPrevisto() { return valorPrevisto; }
    public int getLeiturasUsadas() { return leiturasUsadas; }
    public boolean isConfiavel() { return confiavel; }
    public String getInterpretacao() { return interpretacao; }

    @Override
    public String toString() {
        if (!confiavel) {
            return sensorId + " | " + estrategia + " | " + interpretacao;
        }
        return String.format("%s | %s | previsto: %.2f %s | %s",
                sensorId, estrategia, valorPrevisto, tipo.getUnidade(), interpretacao);
    }
}
