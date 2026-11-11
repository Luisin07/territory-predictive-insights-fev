package br.com.fiap.territorio.dominio;

/**
 * Tipos de sensor do recorte "chuva intensa e risco de alagamento".
 * Cada tipo conhece sua unidade e a faixa FÍSICA aceitável da leitura:
 * fora dela, a leitura é considerada defeito do sensor, não um evento real.
 */
public enum TipoSensor {

    PLUVIOMETRO("Pluviômetro", "mm/h", 0.0, 200.0),
    NIVEL_RIO("Nível do córrego", "m", 0.0, 15.0),
    UMIDADE_SOLO("Umidade do solo", "%", 0.0, 100.0);

    private final String descricao;
    private final String unidade;
    private final double minimoFisico;
    private final double maximoFisico;

    TipoSensor(String descricao, String unidade, double minimoFisico, double maximoFisico) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.minimoFisico = minimoFisico;
        this.maximoFisico = maximoFisico;
    }

    public boolean estaNaFaixaFisica(double valor) {
        return !Double.isNaN(valor) && valor >= minimoFisico && valor <= maximoFisico;
    }

    public String getDescricao() { return descricao; }
    public String getUnidade() { return unidade; }
    public double getMinimoFisico() { return minimoFisico; }
    public double getMaximoFisico() { return maximoFisico; }
}
