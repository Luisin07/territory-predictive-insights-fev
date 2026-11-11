package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.FonteIndisponivelException;

/**
 * Contrato de qualquer sensor do sistema. O serviço só conhece esta
 * interface; não sabe (nem precisa saber) qual sensor concreto está lendo.
 */
public interface Sensor {

    String getId();

    /** Lê o valor bruto. Pode lançar FonteIndisponivelException se o sensor falhar. */
    double lerValor() throws FonteIndisponivelException;

    boolean estaFuncionando();

    TipoSensor getTipo();

    /** Coloca o sensor de volta em operação. */
    void ativar();

    /** Tira o sensor de operação (manutenção); leituras passam a falhar. */
    void desativar();
}
