package br.com.fiap.territorio.persistencia;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.TipoSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de INTEGRAÇÃO com H2 em memória (cada teste ganha um banco novo).
 * Confere que o SQL dos DAOs funciona de verdade, não só a lógica.
 */
class MedicaoDAOTest {

    private MedicaoDAO medicaoDAO;
    private AlertaDAO alertaDAO;

    @BeforeEach
    void preparar() {
        String url = "jdbc:h2:mem:teste_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        ConexaoBanco conexao = new ConexaoBanco(url);
        conexao.inicializarEsquema();
        medicaoDAO = new MedicaoDAO(conexao);
        alertaDAO = new AlertaDAO(conexao);
    }

    @Test
    @DisplayName("Grava medições e devolve as últimas N em ordem cronológica, só do sensor pedido")
    void deveGravarEBuscarUltimasEmOrdemCronologica() {
        // Arrange
        LocalDateTime t = LocalDateTime.of(2026, 11, 11, 10, 0);
        medicaoDAO.salvar(new Medicao("PLV-01", TipoSensor.PLUVIOMETRO, 1.0, t));
        medicaoDAO.salvar(new Medicao("PLV-01", TipoSensor.PLUVIOMETRO, 2.0, t.plusMinutes(1)));
        medicaoDAO.salvar(new Medicao("NIV-01", TipoSensor.NIVEL_RIO, 9.0, t.plusMinutes(2)));
        medicaoDAO.salvar(new Medicao("PLV-01", TipoSensor.PLUVIOMETRO, 3.0, t.plusMinutes(3)));

        // Act
        List<Medicao> ultimas = medicaoDAO.buscarUltimasPorSensor("PLV-01", 2);

        // Assert
        assertEquals(2, ultimas.size());
        assertEquals(2.0, ultimas.get(0).getValor(), 0.0001);
        assertEquals(3.0, ultimas.get(1).getValor(), 0.0001);
        assertEquals(4, medicaoDAO.contar());
        assertNotNull(ultimas.get(0).getId(), "o id gerado pelo banco deve voltar preenchido");
    }

    @Test
    @DisplayName("Alerta gravado volta do banco com nível, limite e estratégia")
    void deveGravarEListarAlerta() {
        // Arrange
        Alerta alerta = new Alerta(null, "PLV-01", TipoSensor.PLUVIOMETRO, NivelAlerta.CRITICO,
                62.0, 50.0, "Média Móvel", LocalDateTime.of(2026, 11, 11, 10, 0));

        // Act
        alertaDAO.salvar(alerta);
        List<Alerta> alertas = alertaDAO.listarTodos();

        // Assert
        assertEquals(1, alertas.size());
        assertEquals(NivelAlerta.CRITICO, alertas.get(0).getNivel());
        assertEquals(50.0, alertas.get(0).getLimiteEstourado(), 0.0001);
        assertEquals("Média Móvel", alertas.get(0).getEstrategia());
    }
}
