package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.AcessoNegadoException;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    private ConfiguracaoSistema configuracao;
    private DadosTerritorio territorio;

    @BeforeEach
    void preparar() {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes(); // o Singleton é compartilhado: cada teste começa limpo
        territorio = new DadosTerritorio("Território Teste", "Setor A", 1000, 2, "CODIGO123", configuracao);
    }

    @Test
    @DisplayName("População negativa lança DadosInvalidosException e mantém o valor antigo")
    void deveRejeitarPopulacaoNegativa() {
        // Arrange
        int populacaoInvalida = -50;

        // Act
        assertThrows(DadosInvalidosException.class, () -> territorio.setPopulacaoEstimada(populacaoInvalida));

        // Assert
        assertEquals(1000, territorio.getPopulacaoEstimada());
    }

    @Test
    @DisplayName("Código de acesso errado não altera os limiares")
    void naoDeveAlterarLimiarComCodigoErrado() {
        // Arrange
        double criticoAntes = configuracao.getLimiar(TipoSensor.PLUVIOMETRO, NivelAlerta.CRITICO);

        // Act
        assertThrows(AcessoNegadoException.class,
                () -> territorio.alterarLimiares("errado", TipoSensor.PLUVIOMETRO, 1, 2, 3));

        // Assert
        assertEquals(criticoAntes, configuracao.getLimiar(TipoSensor.PLUVIOMETRO, NivelAlerta.CRITICO), 0.0001);
    }

    @Test
    @DisplayName("Código certo altera os limiares na ConfiguracaoSistema")
    void deveAlterarLimiarComCodigoCerto() {
        // Arrange
        String codigo = "CODIGO123";

        // Act
        territorio.alterarLimiares(codigo, TipoSensor.PLUVIOMETRO, 15, 35, 60);

        // Assert
        assertEquals(60.0, configuracao.getLimiar(TipoSensor.PLUVIOMETRO, NivelAlerta.CRITICO), 0.0001);
    }
}
