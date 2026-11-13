package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.PrevisaoComparacao;
import br.com.fiap.territorio.dominio.PrevisaoMediaMovel;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricaPrevisaoTest {

    private final FabricaPrevisao fabrica = new FabricaPrevisao();

    @Test
    @DisplayName("Factory cria a estratégia certa a partir do código da configuração")
    void deveCriarEstrategiaPeloCodigo() {
        // Arrange
        String codigo = FabricaPrevisao.COMPARACAO;

        // Act
        EstrategiaPrevisao estrategia = fabrica.criar(codigo);

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    @Test
    @DisplayName("Factory cria todas as estratégias ativas da configuração, na ordem")
    void deveCriarEstrategiasAtivas() {
        // Arrange
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();

        // Act
        List<EstrategiaPrevisao> ativas = fabrica.criarAtivas(configuracao);

        // Assert
        assertEquals(2, ativas.size());
        assertInstanceOf(PrevisaoMediaMovel.class, ativas.get(0));
        assertInstanceOf(PrevisaoComparacao.class, ativas.get(1));
    }

    @Test
    @DisplayName("Código desconhecido lança DadosInvalidosException")
    void deveRejeitarCodigoDesconhecido() {
        // Arrange
        String codigo = "BOLA_DE_CRISTAL";

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> fabrica.criar(codigo));
    }
}
