package br.com.fiap.territorio.excecao;

/**
 * Lançada quando um dado do território não pode ser aceito:
 * medição fora da faixa física do sensor, população negativa,
 * limiar fora de ordem etc.
 *
 * É unchecked (RuntimeException) porque representa um dado errado que
 * chegou até a validação; os setters e construtores lançam, e o menu captura.
 */
public class DadosInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
