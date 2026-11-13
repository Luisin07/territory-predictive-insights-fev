package br.com.fiap.territorio.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Utilitário de teste: monta um histórico cronológico a partir de valores. */
public final class HistoricoFake {

    private HistoricoFake() { }

    public static List<Medicao> de(String sensorId, TipoSensor tipo, double... valores) {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 11, 8, 0);
        List<Medicao> lista = new ArrayList<>();
        for (int i = 0; i < valores.length; i++) {
            lista.add(new Medicao((long) i + 1, sensorId, tipo, valores[i], inicio.plusMinutes(10L * i)));
        }
        return lista;
    }
}
