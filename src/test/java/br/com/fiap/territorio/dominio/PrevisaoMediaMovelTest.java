package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoMediaMovelTest {

    private final PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(); // janela = 5

    @Test
    @DisplayName("Usa só as 5 leituras mais recentes: (10+20+30+40+50)/5 = 30")
    void deveCalcularMediaDasUltimasCincoLeituras() {
        // Arrange: as duas primeiras (100) são antigas e NÃO podem entrar na conta
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO,
                100, 100, 10, 20, 30, 40, 50);

        // Act
        Previsao previsao = estrategia.prever("PLV-01", TipoSensor.PLUVIOMETRO, historico);

        // Assert
        assertTrue(previsao.isConfiavel());
        assertEquals(30.0, previsao.getValorPrevisto(), 0.0001);
        assertEquals(5, previsao.getLeiturasUsadas());
    }

    @Test
    @DisplayName("Com menos de 5 leituras o sistema se cala (previsão não confiável)")
    void deveFicarEmSilencioComPoucasLeituras() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO, 80, 90, 95, 99);

        // Act
        Previsao previsao = estrategia.prever("PLV-01", TipoSensor.PLUVIOMETRO, historico);

        // Assert
        assertFalse(previsao.isConfiavel());
        assertTrue(Double.isNaN(previsao.getValorPrevisto()));
    }

    @Test
    @DisplayName("Histórico nulo lança DadosInvalidosException")
    void deveRejeitarHistoricoNulo() {
        // Arrange
        List<Medicao> historico = null;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> estrategia.prever("PLV-01", TipoSensor.PLUVIOMETRO, historico));
    }
}
