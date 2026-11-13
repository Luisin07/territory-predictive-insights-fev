package br.com.fiap.territorio;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.Pluviometro;
import br.com.fiap.territorio.dominio.PontoMonitoramento;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.SensorNivelRio;
import br.com.fiap.territorio.dominio.SensorUmidadeSolo;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.AcessoNegadoException;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.excecao.PersistenciaException;
import br.com.fiap.territorio.persistencia.AlertaDAO;
import br.com.fiap.territorio.persistencia.ConexaoBanco;
import br.com.fiap.territorio.persistencia.MedicaoDAO;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;
import br.com.fiap.territorio.servico.ExportadorCsv;
import br.com.fiap.territorio.servico.FabricaPrevisao;
import br.com.fiap.territorio.servico.ResultadoPrevisao;
import br.com.fiap.territorio.servico.ServicoMonitoramento;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Menu de console. É a ÚNICA classe que conversa com o usuário:
 * lê o teclado, chama o serviço e transforma exceções em mensagens claras.
 */
public class SistemaMonitoramento {

    /** Código para alterar limiares (dado de configuração, não é dado pessoal). */
    private static final String CODIGO_ACESSO = "DEFESA2026";

    private final Scanner scanner = new Scanner(System.in);
    private final ServicoMonitoramento servico;
    private final DadosTerritorio territorio;
    private final ConfiguracaoSistema configuracao;

    public SistemaMonitoramento(ServicoMonitoramento servico, DadosTerritorio territorio,
                                ConfiguracaoSistema configuracao) {
        this.servico = servico;
        this.territorio = territorio;
        this.configuracao = configuracao;
    }

    public static void main(String[] args) {
        ConexaoBanco conexao = new ConexaoBanco();
        try {
            conexao.inicializarEsquema();
        } catch (PersistenciaException e) {
            System.err.println("ERRO ao preparar o banco de dados: " + e.getMessage());
            System.err.println("Causa: " + e.getCause().getMessage());
            System.err.println("Verifique se outra janela do programa não está com o banco aberto.");
            return;
        }

        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
        DadosTerritorio territorio = new DadosTerritorio(
                "Várzea do Córrego Verde", "Zona Leste (território fictício)",
                18_500, 7, CODIGO_ACESSO, configuracao);

        ServicoMonitoramento servico = new ServicoMonitoramento(
                new MedicaoDAO(conexao), new AlertaDAO(conexao), configuracao,
                new FabricaPrevisao(), montarPontos(new Random()));

        System.out.println("Banco de dados: " + conexao.getUrl());
        new SistemaMonitoramento(servico, territorio, configuracao).executar();
    }

    /** Monta os pontos de monitoramento do território e seus sensores. */
    private static List<PontoMonitoramento> montarPontos(Random random) {
        PontoMonitoramento margem = new PontoMonitoramento(
                "P01", "Margem do córrego - ponte principal", "Setor Norte");
        margem.adicionarSensor(new Pluviometro("PLV-01", random));
        margem.adicionarSensor(new SensorNivelRio("NIV-01", random));
        margem.adicionarSensor(new SensorUmidadeSolo("UMD-01", random));

        PontoMonitoramento encosta = new PontoMonitoramento(
                "P02", "Encosta da vila alta", "Setor Sul");
        encosta.adicionarSensor(new Pluviometro("PLV-02", random));
        encosta.adicionarSensor(new SensorUmidadeSolo("UMD-02", random));

        return List.of(margem, encosta);
    }

    // =================================================================== loop principal

    public void executar() {
        System.out.println("\nTERRITORY PREDICTIVE INSIGHTS - Monitoramento de alagamentos");
        System.out.println("Território: " + territorio.getNome() + " | " + territorio.getRegiao());

        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            if (!scanner.hasNextLine()) {
                break; // entrada encerrada (Ctrl+D / fim de arquivo)
            }
            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1" -> verificarSensores();
                    case "2" -> rodarPrevisoes();
                    case "3" -> verHistoricoPorSensor();
                    case "4" -> verAlertas();
                    case "5" -> gerenciarTerritorio();
                    case "6" -> System.out.println(servico.gerarStatusCompleto(territorio));
                    case "7" -> exportarCsv();
                    case "0" -> rodando = false;
                    default -> System.out.println("[!] Opção '" + opcao + "' não existe. Digite um número de 0 a 7.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("[X] Dado inválido: " + e.getMessage());
            } catch (AcessoNegadoException e) {
                System.out.println("[ACESSO] Acesso negado: " + e.getMessage());
            } catch (PersistenciaException e) {
                System.out.println("[BANCO] Erro no banco de dados: " + e.getMessage()
                        + " (causa: " + e.getCause().getMessage() + ")");
            } catch (NoSuchElementException e) {
                System.out.println("\nEntrada encerrada no meio de uma operação: " + e.getMessage());
                rodando = false;
            }
        }
        System.out.println("Encerrando. O histórico continua salvo no banco para a próxima execução.");
    }

    private void exibirMenu() {
        System.out.println("""

                ------------------------------------------------
                 1 - Verificar sensores (ler e gravar medições)
                 2 - Rodar previsões e alertas
                 3 - Ver histórico por sensor
                 4 - Ver alertas emitidos
                 5 - Gerenciar dados do território
                 6 - Status completo
                 7 - Exportar histórico de um sensor em CSV (bônus)
                 0 - Sair
                ------------------------------------------------""");
        System.out.print("Escolha: ");
    }

    // =================================================================== 1. sensores

    private void verificarSensores() {
        int ciclos = lerInteiro("Quantos ciclos de leitura? (1 a 50, Enter = 1): ", 1, 50, 1);
        int gravadas = 0;
        int falhas = 0;
        for (int ciclo = 1; ciclo <= ciclos; ciclo++) {
            if (ciclos > 1) {
                System.out.println("-- Ciclo " + ciclo + " --");
            }
            for (Sensor sensor : servico.getSensores()) {
                try {
                    Medicao m = servico.lerEGravar(sensor);
                    System.out.println("  [OK] " + m);
                    gravadas++;
                } catch (FonteIndisponivelException e) {
                    System.out.println("  [!] Fonte indisponível: " + e.getMessage());
                    falhas++;
                } catch (DadosInvalidosException e) {
                    System.out.println("  [X] Leitura descartada: " + e.getMessage());
                    falhas++;
                }
            }
        }
        System.out.printf("Resumo: %d medição(ões) gravada(s), %d leitura(s) com problema (não gravadas).%n",
                gravadas, falhas);
    }

    // =================================================================== 2. previsões

    private void rodarPrevisoes() {
        System.out.println("Estratégias ativas:");
        servico.getEstrategiasAtivas().forEach(e -> System.out.println("  - " + e.descrever()));

        int alertas = 0;
        for (Sensor sensor : servico.getSensores()) {
            System.out.println("\n> " + sensor);
            for (ResultadoPrevisao r : servico.preverEAlertar(sensor)) {
                System.out.println("   " + r.getPrevisao());
                if (r.getAlerta().isPresent()) {
                    System.out.println("   >>> " + r.getAlerta().get().getJustificativa());
                    alertas++;
                } else if (r.getPrevisao().isConfiavel()) {
                    System.out.println("   [OK] Dentro dos limiares, nenhum alerta.");
                }
            }
        }
        System.out.println("\nTotal de alertas emitidos nesta rodada: " + alertas
                + ". Os alertas apoiam a decisão; quem decide é a equipe responsável.");
    }

    // =================================================================== 3/4. histórico

    private void verHistoricoPorSensor() {
        String id = escolherSensor();
        List<Medicao> historico = servico.buscarHistoricoPorSensor(id);
        if (historico.isEmpty()) {
            System.out.println("Nenhuma medição gravada para " + id + ". Use a opção 1 primeiro.");
            return;
        }
        System.out.println("Histórico de " + id + " (" + historico.size() + " medições):");
        historico.forEach(m -> System.out.println("  " + m));
    }

    private void verAlertas() {
        List<Alerta> alertas = servico.listarAlertas();
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta emitido até agora.");
            return;
        }
        System.out.println("Alertas emitidos (" + alertas.size() + "):");
        alertas.forEach(a -> System.out.println("  " + a));
    }

    private void exportarCsv() {
        String id = escolherSensor();
        List<Medicao> historico = servico.buscarHistoricoPorSensor(id);
        if (historico.isEmpty()) {
            System.out.println("Nada para exportar: " + id + " ainda não tem medições.");
            return;
        }
        String carimbo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path destino = Path.of("exportacoes", "historico_" + id + "_" + carimbo + ".csv");
        try {
            Path gerado = new ExportadorCsv().exportar(historico, destino);
            System.out.println("[OK] " + historico.size() + " medições exportadas para " + gerado.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("[X] Não foi possível gravar o CSV em " + destino + ": " + e.getMessage());
        }
    }

    // =================================================================== 5. território

    private void gerenciarTerritorio() {
        System.out.printf("""
                Território: %s | %s
                População estimada: %,d | Pontos vulneráveis: %d
                  a - Alterar população estimada
                  b - Alterar quantidade de pontos vulneráveis
                  c - Alterar limiares de alerta (exige código de acesso)
                  d - Ativar/desativar sensor (manutenção)
                  v - Voltar
                Escolha:\s""",
                territorio.getNome(), territorio.getRegiao(),
                territorio.getPopulacaoEstimada(), territorio.getPontosVulneraveis());

        switch (scanner.nextLine().trim().toLowerCase()) {
            case "a" -> {
                territorio.setPopulacaoEstimada(lerInteiroLivre("Nova população estimada: "));
                System.out.println("[OK] População atualizada.");
            }
            case "b" -> {
                territorio.setPontosVulneraveis(lerInteiroLivre("Novos pontos vulneráveis: "));
                System.out.println("[OK] Pontos vulneráveis atualizados.");
            }
            case "c" -> alterarLimiares();
            case "d" -> alternarSensor();
            case "v" -> { /* volta ao menu principal */ }
            default -> System.out.println("[!] Opção inválida, voltando ao menu principal.");
        }
    }

    private void alterarLimiares() {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("  %d - %s (atual: %.2f / %.2f / %.2f %s)%n", i + 1, tipos[i].getDescricao(),
                    configuracao.getLimiar(tipos[i], NivelAlerta.ATENCAO),
                    configuracao.getLimiar(tipos[i], NivelAlerta.ALERTA),
                    configuracao.getLimiar(tipos[i], NivelAlerta.CRITICO), tipos[i].getUnidade());
        }
        TipoSensor tipo = tipos[lerInteiro("Tipo de sensor: ", 1, tipos.length, null) - 1];
        System.out.print("Código de acesso: ");
        String codigo = scanner.nextLine().trim();
        territorio.verificarAcesso(codigo); // recusa antes de pedir os valores
        double atencao = lerDecimal("Limiar de ATENÇÃO: ");
        double alerta = lerDecimal("Limiar de ALERTA: ");
        double critico = lerDecimal("Limiar de CRÍTICO: ");
        territorio.alterarLimiares(codigo, tipo, atencao, alerta, critico);
        System.out.println("[OK] Limiares de " + tipo.getDescricao() + " atualizados.");
    }

    private void alternarSensor() {
        Sensor sensor = servico.buscarSensor(escolherSensor());
        if (sensor.estaFuncionando()) {
            sensor.desativar();
            System.out.println("[OK] " + sensor.getId() + " desativado para manutenção.");
        } else {
            sensor.ativar();
            System.out.println("[OK] " + sensor.getId() + " reativado.");
        }
    }

    // =================================================================== utilitários de entrada

    private String escolherSensor() {
        System.out.println("Sensores disponíveis:");
        servico.getSensores().forEach(s -> System.out.println("  " + s));
        System.out.print("ID do sensor (ex.: PLV-01): ");
        String id = scanner.nextLine().trim().toUpperCase();
        servico.buscarSensor(id); // lança DadosInvalidosException se não existir
        return id;
    }

    /** Lê um inteiro dentro de [min, max]; repete até o usuário acertar. */
    private int lerInteiro(String pergunta, int min, int max, Integer padrao) {
        while (true) {
            System.out.print(pergunta);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty() && padrao != null) {
                return padrao;
            }
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("  [!] Digite um número entre " + min + " e " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] '" + texto + "' não é um número inteiro. Tente de novo.");
            }
        }
    }

    /** Lê um inteiro sem faixa: quem valida o significado (ex.: população negativa) é o domínio. */
    private int lerInteiroLivre(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            String texto = scanner.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  [!] '" + texto + "' não é um número inteiro. Tente de novo.");
            }
        }
    }

    private double lerDecimal(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            String texto = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("  [!] '" + texto + "' não é um número. Use, por exemplo, 30 ou 30,5.");
            }
        }
    }
}
