package br.com.fiap.territorio.dominio;

import java.util.List;

/**
 * Previsão = média das últimas N leituras.
 * Suaviza ruído: um pico isolado não dispara alerta sozinho.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    public static final int JANELA_PADRAO = 5;

    private final int janela;

    public PrevisaoMediaMovel() {
        this(JANELA_PADRAO);
    }

    public PrevisaoMediaMovel(int janela) {
        super("Média Móvel", janela);   // a janela é também o mínimo de leituras
        this.janela = janela;
    }

    @Override
    public Previsao prever(String sensorId, TipoSensor tipo, List<Medicao> historico) {
        if (!temLeiturasSuficientes(historico)) {
            return Previsao.insuficiente(sensorId, tipo, getNome(), historico.size(), getMinimoLeituras());
        }
        List<Medicao> ultimas = historico.subList(historico.size() - janela, historico.size());
        double previsto = arredondar(media(ultimas));

        String interpretacao = String.format(
                "Média das últimas %d leituras: espera-se que o valor fique em torno de %.2f %s.",
                janela, previsto, tipo.getUnidade());
        return Previsao.confiavel(sensorId, tipo, getNome(), previsto, janela, interpretacao);
    }

    @Override
    public String descrever() {
        return super.descrever() + " - suaviza picos isolados usando as últimas " + janela + " leituras";
    }
}
