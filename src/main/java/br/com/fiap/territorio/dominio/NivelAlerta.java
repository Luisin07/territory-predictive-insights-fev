package br.com.fiap.territorio.dominio;

/**
 * Níveis de alerta em ordem crescente de gravidade.
 * A ordem da declaração importa: ordinal() é usado para comparar gravidade.
 */
public enum NivelAlerta {

    ATENCAO("ATENÇÃO", "Acompanhar a evolução e revisar o plano de contingência."),
    ALERTA("ALERTA", "Avisar moradores dos pontos vulneráveis e preparar abrigos."),
    CRITICO("CRÍTICO", "Acionar a Defesa Civil e orientar a saída preventiva das áreas de risco.");

    private final String rotulo;
    private final String acaoRecomendada;

    NivelAlerta(String rotulo, String acaoRecomendada) {
        this.rotulo = rotulo;
        this.acaoRecomendada = acaoRecomendada;
    }

    public String getRotulo() { return rotulo; }
    public String getAcaoRecomendada() { return acaoRecomendada; }
}
