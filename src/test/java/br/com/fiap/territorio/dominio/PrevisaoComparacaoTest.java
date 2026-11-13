package br.com.fiap.territorio.dominio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoComparacaoTest {

    private final PrevisaoComparacao estrategia = new PrevisaoComparacao(); // 2 blocos de 3

    @Test
    @DisplayName("Tendência de alta: anterior 10, atual 20 -> projeta 30")
    void deveProjetarTendenciaDeAlta() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO,
                10, 10, 10, 20, 20, 20);

        // Act
        Previsao previsao = estrategia.prever("PLV-01", TipoSensor.PLUVIOMETRO, historico);

        // Assert
        assertTrue(previsao.isConfiavel());
        assertEquals(30.0, previsao.getValorPrevisto(), 0.0001);
        assertTrue(previsao.getInterpretacao().contains("subindo"));
    }

    @Test
    @DisplayName("Queda forte não gera previsão negativa: 30 -> 10 projeta 0, não -10")
    void naoDevePreverValorNegativo() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO,
                30, 30, 30, 10, 10, 10);

        // Act
        Previsao previsao = estrategia.prever("PLV-01", TipoSensor.PLUVIOMETRO, historico);

        // Assert
        assertEquals(0.0, previsao.getValorPrevisto(), 0.0001);
    }

    @Test
    @DisplayName("Precisa de 6 leituras: com 5 o sistema se cala")
    void deveFicarEmSilencioSemDoisPeriodosCompletos() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("NIV-01", TipoSensor.NIVEL_RIO, 1, 2, 3, 4, 5);

        // Act
        Previsao previsao = estrategia.prever("NIV-01", TipoSensor.NIVEL_RIO, historico);

        // Assert
        assertFalse(previsao.isConfiavel());
    }
}
