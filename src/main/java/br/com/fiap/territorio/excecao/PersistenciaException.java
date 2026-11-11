package br.com.fiap.territorio.excecao;

/**
 * Embrulha um erro de banco (SQLException) para que as camadas de cima
 * não precisem conhecer JDBC, sem perder a causa original.
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
