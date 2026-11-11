package br.com.fiap.territorio.dominio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Alerta emitido quando uma previsão estoura um limiar.
 * Guarda tudo o que explica o porquê: sensor, valor previsto,
 * limite estourado e qual estratégia gerou a previsão.
 */
public class Alerta {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    private Long id;
    private final String sensorId;
    private final TipoSensor tipo;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limiteEstourado;
    private final String estrategia;
    private final LocalDateTime dataHora;

    public Alerta(Long id, String sensorId, TipoSensor tipo, NivelAlerta nivel, double valorPrevisto,
                  double limiteEstourado, String estrategia, LocalDateTime dataHora) {
        this.id = id;
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiteEstourado = limiteEstourado;
        this.estrategia = estrategia;
        this.dataHora = dataHora;
    }

    /** Frase que explica o alerta para uma pessoa, não para uma máquina. */
    public String getJustificativa() {
        return String.format(
                "%s: %s (%s) com previsão de %.2f %s pela %s, atingindo o limite de %s (%.2f %s). Ação sugerida: %s",
                nivel.getRotulo(), sensorId, tipo.getDescricao(), valorPrevisto, tipo.getUnidade(),
                estrategia, nivel.getRotulo(), limiteEstourado, tipo.getUnidade(), nivel.getAcaoRecomendada());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSensorId() { return sensorId; }
    public TipoSensor getTipo() { return tipo; }
    public NivelAlerta getNivel() { return nivel; }
    public double getValorPrevisto() { return valorPrevisto; }
    public double getLimiteEstourado() { return limiteEstourado; }
    public String getEstrategia() { return estrategia; }
    public LocalDateTime getDataHora() { return dataHora; }

    @Override
    public String toString() {
        return "[" + dataHora.format(FORMATO) + "] " + getJustificativa();
    }
}
