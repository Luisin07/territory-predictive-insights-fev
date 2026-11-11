package br.com.fiap.territorio.dominio;

import java.util.List;

/**
 * Compara o período atual (últimas N leituras) com o período anterior (N antes delas).
 * Se está subindo, projeta que vai continuar subindo no mesmo ritmo:
 *     previsto = mediaAtual + (mediaAtual - mediaAnterior)
 * Reage mais rápido que a média móvel quando a chuva está apertando.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    public static final int JANELA_PADRAO = 3;

    private final int janela;

    public PrevisaoComparacao() {
        this(JANELA_PADRAO);
    }

    public PrevisaoComparacao(int janela) {
        super("Comparação de Períodos", janela * 2);   // precisa de dois períodos completos
        this.janela = janela;
    }

    @Override
    public Previsao prever(String sensorId, TipoSensor tipo, List<Medicao> historico) {
        if (!temLeiturasSuficientes(historico)) {
            return Previsao.insuficiente(sensorId, tipo, getNome(), historico.size(), getMinimoLeituras());
        }
        int fim = historico.size();
        double mediaAtual = media(historico.subList(fim - janela, fim));
        double mediaAnterior = media(historico.subList(fim - 2 * janela, fim - janela));
        double variacao = mediaAtual - mediaAnterior;

        // nenhum sensor deste recorte mede valor negativo
        double previsto = arredondar(Math.max(tipo.getMinimoFisico(), mediaAtual + variacao));

        String tendencia = descreverTendencia(variacao, mediaAnterior);
        String interpretacao = String.format(
                "Período atual (média %.2f) vs anterior (média %.2f): %s. Mantido o ritmo, chega a %.2f %s.",
                mediaAtual, mediaAnterior, tendencia, previsto, tipo.getUnidade());
        return Previsao.confiavel(sensorId, tipo, getNome(), previsto, janela * 2, interpretacao);
    }

    private String descreverTendencia(double variacao, double mediaAnterior) {
        if (mediaAnterior == 0) {
            return variacao > 0 ? "subindo a partir de zero" : "estável em zero";
        }
        double percentual = variacao / mediaAnterior * 100.0;
        if (Math.abs(percentual) < 5) {
            return String.format("estável (%+.1f%%)", percentual);
        }
        return String.format("%s %.1f%%", percentual > 0 ? "subindo" : "caindo", Math.abs(percentual));
    }

    @Override
    public String descrever() {
        return super.descrever() + " - projeta a tendência comparando dois blocos de " + janela + " leituras";
    }
}
