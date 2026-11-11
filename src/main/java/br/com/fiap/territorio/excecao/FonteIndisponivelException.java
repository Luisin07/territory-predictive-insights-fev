package br.com.fiap.territorio.excecao;

/**
 * Lançada quando uma fonte de dados (sensor, gerador de leituras) falha.
 *
 * É checked (Exception) de propósito: sensor falhar em campo é algo
 * esperado, então o compilador obriga quem lê um sensor a tratar a falha.
 */
public class FonteIndisponivelException extends Exception {

    private static final long serialVersionUID = 1L;

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
