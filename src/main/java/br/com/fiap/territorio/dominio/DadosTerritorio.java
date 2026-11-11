package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.AcessoNegadoException;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;

/**
 * Dados gerais do território monitorado. Todos os atributos são privados:
 * a única forma de mudar algo é pelos métodos, que validam antes.
 *
 * Os limiares de alerta são sensíveis (mudar um limiar pode silenciar um alerta
 * de enchente), por isso só podem ser alterados com o código de acesso.
 */
public final class DadosTerritorio {

    private final String nome;
    private final String regiao;
    private int populacaoEstimada;
    private int pontosVulneraveis;
    private final String codigoAcesso;
    private final ConfiguracaoSistema configuracao;

    public DadosTerritorio(String nome, String regiao, int populacaoEstimada, int pontosVulneraveis,
                           String codigoAcesso, ConfiguracaoSistema configuracao) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O território precisa de um nome.");
        }
        if (codigoAcesso == null || codigoAcesso.length() < 6) {
            throw new DadosInvalidosException("O código de acesso precisa ter pelo menos 6 caracteres.");
        }
        this.nome = nome;
        this.regiao = regiao;
        this.codigoAcesso = codigoAcesso;
        this.configuracao = configuracao;
        setPopulacaoEstimada(populacaoEstimada);
        setPontosVulneraveis(pontosVulneraveis);
    }

    public void setPopulacaoEstimada(int populacaoEstimada) {
        if (populacaoEstimada < 0) {
            throw new DadosInvalidosException(
                    "População não pode ser negativa (recebido: " + populacaoEstimada + ").");
        }
        this.populacaoEstimada = populacaoEstimada;
    }

    public void setPontosVulneraveis(int pontosVulneraveis) {
        if (pontosVulneraveis < 0) {
            throw new DadosInvalidosException(
                    "Quantidade de pontos vulneráveis não pode ser negativa (recebido: " + pontosVulneraveis + ").");
        }
        this.pontosVulneraveis = pontosVulneraveis;
    }

    /**
     * Altera os três limiares de um tipo de sensor de uma vez,
     * para que a ordem ATENÇÃO < ALERTA < CRÍTICO possa ser validada junto.
     */
    public void alterarLimiares(String codigoInformado, TipoSensor tipo,
                                double atencao, double alerta, double critico) {
        verificarAcesso(codigoInformado);
        configuracao.definirLimiares(tipo, atencao, alerta, critico);
    }

    /** Lança AcessoNegadoException se o código não conferir. Não revela qual é o código certo. */
    public void verificarAcesso(String codigoInformado) {
        if (!codigoAcesso.equals(codigoInformado)) {
            throw new AcessoNegadoException("Código de acesso incorreto: limiares não foram alterados.");
        }
    }

    public String getNome() { return nome; }
    public String getRegiao() { return regiao; }
    public int getPopulacaoEstimada() { return populacaoEstimada; }
    public int getPontosVulneraveis() { return pontosVulneraveis; }
    // de propósito NÃO existe getCodigoAcesso()
}
