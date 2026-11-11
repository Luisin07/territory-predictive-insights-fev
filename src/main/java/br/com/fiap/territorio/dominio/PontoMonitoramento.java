package br.com.fiap.territorio.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Um ponto físico do território (ex.: margem de um córrego) com seus sensores.
 * O status depende de quantos sensores estão funcionando.
 */
public class PontoMonitoramento extends ComponenteTerritorial {

    private final List<Sensor> sensores = new ArrayList<>();

    public PontoMonitoramento(String id, String nome, String regiao) {
        super(id, nome, regiao);
    }

    public void adicionarSensor(Sensor sensor) {
        sensores.add(sensor);
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    public long contarSensoresFuncionando() {
        return sensores.stream().filter(Sensor::estaFuncionando).count();
    }

    @Override
    public StatusComponente verificarStatus() {
        long funcionando = contarSensoresFuncionando();
        StatusComponente novo;
        if (sensores.isEmpty() || funcionando == 0) {
            novo = StatusComponente.INOPERANTE;
        } else if (funcionando < sensores.size()) {
            novo = StatusComponente.DEGRADADO;
        } else {
            novo = StatusComponente.OPERACIONAL;
        }
        setStatus(novo);
        return novo;
    }
}
