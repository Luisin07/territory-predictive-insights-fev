package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.PontoMonitoramento;
import br.com.fiap.territorio.dominio.Previsao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.AlertaDAO;
import br.com.fiap.territorio.persistencia.MedicaoDAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio: ler sensores, gravar, prever e decidir se há alerta.
 * Não lê teclado nem imprime nada: quem conversa com o usuário é o menu.
 * Recebe os DAOs pelo construtor, o que permite trocá-los por mocks nos testes.
 */
public class ServicoMonitoramento {

    /** Quantas leituras recentes buscar no banco para alimentar as previsões. */
    private static final int LEITURAS_PARA_PREVISAO = 30;

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final ConfiguracaoSistema configuracao;
    private final FabricaPrevisao fabrica;
    private final List<PontoMonitoramento> pontos;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO, ConfiguracaoSistema configuracao,
                                FabricaPrevisao fabrica, List<PontoMonitoramento> pontos) {
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.configuracao = configuracao;
        this.fabrica = fabrica;
        this.pontos = pontos;
    }

    // ------------------------------------------------------------------ leituras

    /**
     * Lê um sensor, valida e grava.
     * @throws FonteIndisponivelException se o sensor não responder (nada é gravado)
     * @throws DadosInvalidosException    se o valor lido for impossível (nada é gravado)
     */
    public Medicao lerEGravar(Sensor sensor) throws FonteIndisponivelException {
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor, LocalDateTime.now());
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    // ------------------------------------------------------------------ previsões e alertas

    /** Roda todas as estratégias ativas sobre o histórico do banco de um sensor. */
    public List<ResultadoPrevisao> preverEAlertar(Sensor sensor) {
        List<Medicao> historico = medicaoDAO.buscarUltimasPorSensor(sensor.getId(), LEITURAS_PARA_PREVISAO);
        List<ResultadoPrevisao> resultados = new ArrayList<>();
        for (EstrategiaPrevisao estrategia : getEstrategiasAtivas()) {
            Previsao previsao = estrategia.prever(sensor.getId(), sensor.getTipo(), historico); // polimorfismo
            Alerta alerta = avaliarPrevisao(previsao).orElse(null);
            resultados.add(new ResultadoPrevisao(previsao, alerta));
        }
        return resultados;
    }

    /**
     * Compara a previsão com os limiares da ConfiguracaoSistema. Se estourar,
     * cria e GRAVA o alerta. Previsão não confiável nunca gera alerta.
     */
    public Optional<Alerta> avaliarPrevisao(Previsao previsao) {
        if (!previsao.isConfiavel()) {
            return Optional.empty();
        }
        Optional<NivelAlerta> nivel = configuracao.classificar(previsao.getTipo(), previsao.getValorPrevisto());
        if (nivel.isEmpty()) {
            return Optional.empty();
        }
        Alerta alerta = new Alerta(null, previsao.getSensorId(), previsao.getTipo(), nivel.get(),
                previsao.getValorPrevisto(), configuracao.getLimiar(previsao.getTipo(), nivel.get()),
                previsao.getEstrategia(), LocalDateTime.now());
        alertaDAO.salvar(alerta);
        return Optional.of(alerta);
    }

    // ------------------------------------------------------------------ consultas

    public List<Medicao> buscarHistoricoPorSensor(String sensorId) {
        buscarSensor(sensorId); // valida se o sensor existe
        return medicaoDAO.buscarPorSensor(sensorId);
    }

    public List<Alerta> listarAlertas() {
        return alertaDAO.listarTodos();
    }

    public Sensor buscarSensor(String sensorId) {
        return getSensores().stream()
                .filter(s -> s.getId().equalsIgnoreCase(sensorId == null ? "" : sensorId.trim()))
                .findFirst()
                .orElseThrow(() -> new DadosInvalidosException("Sensor '" + sensorId + "' não existe no território."));
    }

    public List<Sensor> getSensores() {
        List<Sensor> todos = new ArrayList<>();
        pontos.forEach(p -> todos.addAll(p.getSensores()));
        return todos;
    }

    /** Estratégias que estão valendo agora, criadas pela fábrica a partir da configuração. */
    public List<EstrategiaPrevisao> getEstrategiasAtivas() {
        return fabrica.criarAtivas(configuracao);
    }

    public List<PontoMonitoramento> getPontos() {
        return pontos;
    }

    public Relatorio gerarStatusCompleto(DadosTerritorio territorio) {
        RelatorioBuilder builder = new RelatorioBuilder()
                .comTerritorio(territorio)
                .comPontos(pontos)
                .comLimiares(configuracao)
                .comTotalMedicoes(medicaoDAO.contar())
                .comAlertasRecentes(alertaDAO.listarRecentes(5));
        for (Sensor s : getSensores()) {
            medicaoDAO.buscarUltimaPorSensor(s.getId()).ifPresent(m -> builder.comUltimaLeitura(s.getId(), m));
        }
        return builder.construir();
    }
}
