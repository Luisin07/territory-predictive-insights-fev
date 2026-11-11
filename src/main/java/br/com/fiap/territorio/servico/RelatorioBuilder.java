package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.PontoMonitoramento;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PADRÃO BUILDER.
 *
 * O relatório de status tem várias seções opcionais (pontos, últimas leituras,
 * alertas, limiares). Um construtor com 6 parâmetros, vários deles null, seria
 * ilegível; com o builder cada seção é adicionada pelo nome e só o território é obrigatório.
 */
public class RelatorioBuilder {

    private static final String LINHA = "=".repeat(70);

    private DadosTerritorio territorio;
    private final List<PontoMonitoramento> pontos = new ArrayList<>();
    private final Map<String, Medicao> ultimasLeituras = new LinkedHashMap<>();
    private final List<Alerta> alertas = new ArrayList<>();
    private ConfiguracaoSistema configuracao;
    private long totalMedicoes = -1;

    public RelatorioBuilder comTerritorio(DadosTerritorio territorio) {
        this.territorio = territorio;
        return this;
    }

    public RelatorioBuilder comPontos(List<PontoMonitoramento> pontos) {
        this.pontos.addAll(pontos);
        return this;
    }

    public RelatorioBuilder comUltimaLeitura(String sensorId, Medicao medicao) {
        this.ultimasLeituras.put(sensorId, medicao);
        return this;
    }

    public RelatorioBuilder comAlertasRecentes(List<Alerta> alertas) {
        this.alertas.addAll(alertas);
        return this;
    }

    public RelatorioBuilder comLimiares(ConfiguracaoSistema configuracao) {
        this.configuracao = configuracao;
        return this;
    }

    public RelatorioBuilder comTotalMedicoes(long total) {
        this.totalMedicoes = total;
        return this;
    }

    public Relatorio construir() {
        if (territorio == null) {
            throw new DadosInvalidosException("O relatório precisa do território para ser montado.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(LINHA).append('\n')
          .append(" STATUS COMPLETO - ").append(territorio.getNome()).append('\n')
          .append(LINHA).append('\n')
          .append(String.format(" Região: %s | População estimada: %,d | Pontos vulneráveis: %d%n",
                  territorio.getRegiao(), territorio.getPopulacaoEstimada(), territorio.getPontosVulneraveis()));
        if (totalMedicoes >= 0) {
            sb.append(" Medições no histórico: ").append(totalMedicoes).append('\n');
        }

        if (!pontos.isEmpty()) {
            sb.append("\n PONTOS DE MONITORAMENTO\n");
            for (PontoMonitoramento p : pontos) {
                sb.append(String.format("  - %s | %s (%s) -> %s (%d/%d sensores ativos)%n",
                        p.getId(), p.getNome(), p.getRegiao(), p.verificarStatus(),
                        p.contarSensoresFuncionando(), p.getSensores().size()));
                for (Sensor s : p.getSensores()) {
                    Medicao ultima = ultimasLeituras.get(s.getId());
                    sb.append("      ").append(s)
                      .append(" | última leitura: ").append(ultima == null ? "nenhuma" : ultima)
                      .append('\n');
                }
            }
        }

        if (configuracao != null) {
            sb.append("\n LIMIARES EM VIGOR (ATENÇÃO / ALERTA / CRÍTICO)\n");
            for (TipoSensor tipo : TipoSensor.values()) {
                sb.append(String.format("  - %-17s %6.2f / %6.2f / %6.2f %s%n", tipo.getDescricao(),
                        configuracao.getLimiar(tipo, NivelAlerta.ATENCAO),
                        configuracao.getLimiar(tipo, NivelAlerta.ALERTA),
                        configuracao.getLimiar(tipo, NivelAlerta.CRITICO), tipo.getUnidade()));
            }
        }

        sb.append("\n ALERTAS RECENTES\n");
        if (alertas.isEmpty()) {
            sb.append("  Nenhum alerta registrado.\n");
        } else {
            alertas.forEach(a -> sb.append("  ").append(a).append('\n'));
        }
        sb.append(LINHA);
        return new Relatorio(sb.toString());
    }
}
