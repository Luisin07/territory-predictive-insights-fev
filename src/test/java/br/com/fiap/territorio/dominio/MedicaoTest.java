package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicaoTest {

    @Test
    @DisplayName("Medição dentro da faixa física é criada com os dados informados")
    void deveCriarMedicaoValida() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 11, 14, 0);

        // Act
        Medicao medicao = new Medicao("PLV-01", TipoSensor.PLUVIOMETRO, 35.5, agora);

        // Assert
        assertEquals("PLV-01", medicao.getSensorId());
        assertEquals(35.5, medicao.getValor(), 0.0001);
        assertEquals(agora, medicao.getDataHora());
    }

    @Test
    @DisplayName("Chuva negativa lança DadosInvalidosException")
    void deveRejeitarChuvaNegativa() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("PLV-01", TipoSensor.PLUVIOMETRO, -3.0, agora));

        // Assert
        assertTrue(erro.getMessage().contains("PLV-01"), "a mensagem deve dizer qual sensor falhou");
        assertTrue(erro.getMessage().contains("fora da faixa"), "a mensagem deve explicar o motivo");
    }

    @Test
    @DisplayName("Umidade do solo acima de 100% lança DadosInvalidosException")
    void deveRejeitarUmidadeAcimaDe100() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("UMD-01", TipoSensor.UMIDADE_SOLO, 100.1, agora));
    }

    @Test
    @DisplayName("Valor exatamente no limite da faixa física é aceito")
    void deveAceitarValorNoLimiteDaFaixa() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act
        Medicao medicao = new Medicao("UMD-01", TipoSensor.UMIDADE_SOLO, 100.0, agora);

        // Assert
        assertEquals(100.0, medicao.getValor(), 0.0001);
    }
}
