package br.com.fiap.territorio.excecao;

/**
 * Lançada quando alguém tenta alterar os limiares de alerta
 * sem informar o código de acesso correto.
 */
public class AcessoNegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
