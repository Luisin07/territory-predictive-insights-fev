package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Uma leitura de sensor. A validação acontece no construtor:
 * se o objeto existe, o valor dele é fisicamente possível.
 */
public class Medicao {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    private Long id;                 // preenchido pelo banco
    private final String sensorId;
    private final TipoSensor tipo;
    private final double valor;
    private final LocalDateTime dataHora;

    /** Usado para uma leitura nova (ainda não gravada). */
    public Medicao(String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora) {
        this(null, sensorId, tipo, valor, dataHora);
    }

    /** Usado pelo DAO ao reconstruir uma medição que veio do banco. */
    public Medicao(Long id, String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora) {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("Medição sem identificação de sensor.");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("Medição do sensor " + sensorId + " sem tipo definido.");
        }
        if (!tipo.estaNaFaixaFisica(valor)) {
            throw new DadosInvalidosException(String.format(
                    "Medição inválida do sensor %s: %.2f %s está fora da faixa física de %s (%.0f a %.0f %s).",
                    sensorId, valor, tipo.getUnidade(), tipo.getDescricao(),
                    tipo.getMinimoFisico(), tipo.getMaximoFisico(), tipo.getUnidade()));
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Medição do sensor " + sensorId + " sem data/hora.");
        }
        this.id = id;
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSensorId() { return sensorId; }
    public TipoSensor getTipo() { return tipo; }
    public double getValor() { return valor; }
    public LocalDateTime getDataHora() { return dataHora; }

    @Override
    public String toString() {
        return String.format("[%s] %s = %.2f %s", dataHora.format(FORMATO), sensorId, valor, tipo.getUnidade());
    }
}
