package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracaoSistemaTest {

    private ConfiguracaoSistema configuracao;

    @BeforeEach
    void preparar() {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();
    }

    @Test
    @DisplayName("Singleton: duas chamadas devolvem a MESMA instância")
    void deveSempreDevolverAMesmaInstancia() {
        // Arrange + Act
        ConfiguracaoSistema outra = ConfiguracaoSistema.getInstancia();

        // Assert
        assertSame(configuracao, outra);
    }

    @Test
    @DisplayName("Limiares fora de ordem (ALERTA menor que ATENÇÃO) são rejeitados")
    void deveRejeitarLimiaresForaDeOrdem() {
        // Arrange
        TipoSensor tipo = TipoSensor.NIVEL_RIO;

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> configuracao.definirLimiares(tipo, 5.0, 4.0, 6.0));
    }

    @Test
    @DisplayName("Classifica pelo nível mais grave atingido: 55 mm/h é CRÍTICO, 5 mm/h não alerta")
    void deveClassificarPeloNivelMaisGrave() {
        // Arrange (padrões: 10 / 30 / 50 mm/h)

        // Act
        Optional<NivelAlerta> forte = configuracao.classificar(TipoSensor.PLUVIOMETRO, 55.0);
        Optional<NivelAlerta> fraca = configuracao.classificar(TipoSensor.PLUVIOMETRO, 5.0);
        Optional<NivelAlerta> noLimite = configuracao.classificar(TipoSensor.PLUVIOMETRO, 30.0);

        // Assert
        assertEquals(Optional.of(NivelAlerta.CRITICO), forte);
        assertTrue(fraca.isEmpty());
        assertEquals(Optional.of(NivelAlerta.ALERTA), noLimite);
    }
}
