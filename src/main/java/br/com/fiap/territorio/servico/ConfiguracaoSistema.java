package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PADRÃO SINGLETON.
 *
 * Guarda os limiares de alerta e as estratégias ativas. Precisa ser ÚNICA:
 * se o menu alterasse uma cópia e o serviço lesse outra, o sistema emitiria
 * alertas com um limiar que a equipe acha que já mudou.
 */
public final class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    private final Map<TipoSensor, Map<NivelAlerta, Double>> limiares = new EnumMap<>(TipoSensor.class);
    private final List<String> estrategiasAtivas = new ArrayList<>();

    /** Construtor privado: ninguém de fora consegue dar "new ConfiguracaoSistema()". */
    private ConfiguracaoSistema() {
        restaurarPadroes();
    }

    public static synchronized ConfiguracaoSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    /**
     * Valores de referência. Chuva: a escala usual de intensidade trata ~10 mm/h como
     * moderada, ~30 mm/h como forte e ~50 mm/h como muito forte.
     * Nível do córrego e umidade do solo são valores ilustrativos para o território fictício.
     */
    public void restaurarPadroes() {
        limiares.clear();
        definirLimiares(TipoSensor.PLUVIOMETRO, 10.0, 30.0, 50.0);
        definirLimiares(TipoSensor.NIVEL_RIO, 3.0, 4.5, 6.0);
        definirLimiares(TipoSensor.UMIDADE_SOLO, 70.0, 85.0, 95.0);
        estrategiasAtivas.clear();
        estrategiasAtivas.add(FabricaPrevisao.MEDIA_MOVEL);
        estrategiasAtivas.add(FabricaPrevisao.COMPARACAO);
    }

    public void definirLimiares(TipoSensor tipo, double atencao, double alerta, double critico) {
        if (tipo == null) {
            throw new DadosInvalidosException("Tipo de sensor não informado para o limiar.");
        }
        validarDentroDaFaixa(tipo, atencao);
        validarDentroDaFaixa(tipo, alerta);
        validarDentroDaFaixa(tipo, critico);
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(String.format(
                    "Limiar inválido para %s: é preciso ATENÇÃO (%.2f) < ALERTA (%.2f) < CRÍTICO (%.2f).",
                    tipo.getDescricao(), atencao, alerta, critico));
        }
        Map<NivelAlerta, Double> porNivel = new EnumMap<>(NivelAlerta.class);
        porNivel.put(NivelAlerta.ATENCAO, atencao);
        porNivel.put(NivelAlerta.ALERTA, alerta);
        porNivel.put(NivelAlerta.CRITICO, critico);
        limiares.put(tipo, porNivel);
    }

    private void validarDentroDaFaixa(TipoSensor tipo, double valor) {
        if (!tipo.estaNaFaixaFisica(valor)) {
            throw new DadosInvalidosException(String.format(
                    "Limiar inválido para %s: %.2f %s está fora da faixa física (%.0f a %.0f).",
                    tipo.getDescricao(), valor, tipo.getUnidade(), tipo.getMinimoFisico(), tipo.getMaximoFisico()));
        }
    }

    public double getLimiar(TipoSensor tipo, NivelAlerta nivel) {
        return limiares.get(tipo).get(nivel);
    }

    /**
     * Devolve o nível MAIS GRAVE cujo limiar foi atingido, ou vazio se nenhum foi.
     * Percorre do CRÍTICO para o ATENÇÃO (ordem inversa do enum).
     */
    public Optional<NivelAlerta> classificar(TipoSensor tipo, double valorPrevisto) {
        NivelAlerta[] niveis = NivelAlerta.values();
        for (int i = niveis.length - 1; i >= 0; i--) {
            if (valorPrevisto >= getLimiar(tipo, niveis[i])) {
                return Optional.of(niveis[i]);
            }
        }
        return Optional.empty();
    }

    public List<String> getEstrategiasAtivas() {
        return Collections.unmodifiableList(estrategiasAtivas);
    }
}
