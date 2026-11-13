package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.Previsao;

import java.util.Optional;

/** Junta a previsão feita e o alerta que ela gerou (se gerou). */
public class ResultadoPrevisao {

    private final Previsao previsao;
    private final Alerta alerta;

    public ResultadoPrevisao(Previsao previsao, Alerta alerta) {
        this.previsao = previsao;
        this.alerta = alerta;
    }

    public Previsao getPrevisao() { return previsao; }
    public Optional<Alerta> getAlerta() { return Optional.ofNullable(alerta); }
}
