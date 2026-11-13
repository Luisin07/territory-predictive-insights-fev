package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.HistoricoFake;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.Pluviometro;
import br.com.fiap.territorio.dominio.PontoMonitoramento;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.AlertaDAO;
import br.com.fiap.territorio.persistencia.MedicaoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa a lógica de alerta SEM banco: os DAOs são mocks do Mockito.
 * Assim o teste é rápido e falha só se a regra de negócio estiver errada.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;

    @Mock
    private AlertaDAO alertaDAO;

    private ServicoMonitoramento servico;
    private Sensor pluviometro;

    @BeforeEach
    void preparar() {
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes(); // chuva: 10 / 30 / 50 mm/h

        pluviometro = new Pluviometro("PLV-01", new Random(1));
        PontoMonitoramento ponto = new PontoMonitoramento("P01", "Ponto de teste", "Setor A");
        ponto.adicionarSensor(pluviometro);

        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, configuracao, new FabricaPrevisao(), List.of(ponto));
    }

    @Test
    @DisplayName("Chuva prevista de 60 mm/h gera e grava alerta CRÍTICO nas duas estratégias")
    void deveGravarAlertaCriticoQuandoPrevisaoEstouraLimiar() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO, 60, 60, 60, 60, 60, 60);
        when(medicaoDAO.buscarUltimasPorSensor(eq("PLV-01"), anyInt())).thenReturn(historico);

        // Act
        List<ResultadoPrevisao> resultados = servico.preverEAlertar(pluviometro);

        // Assert
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaDAO, times(2)).salvar(captor.capture());
        for (Alerta alerta : captor.getAllValues()) {
            assertEquals(NivelAlerta.CRITICO, alerta.getNivel());
            assertEquals(50.0, alerta.getLimiteEstourado(), 0.0001);
            assertEquals("PLV-01", alerta.getSensorId());
        }
        assertEquals(2, resultados.size());
    }

    @Test
    @DisplayName("Chuva fraca (2 mm/h) não gera alerta nenhum")
    void naoDeveGravarAlertaDentroDosLimiares() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO, 2, 2, 2, 2, 2, 2);
        when(medicaoDAO.buscarUltimasPorSensor(eq("PLV-01"), anyInt())).thenReturn(historico);

        // Act
        List<ResultadoPrevisao> resultados = servico.preverEAlertar(pluviometro);

        // Assert
        verify(alertaDAO, never()).salvar(any());
        assertTrue(resultados.stream().allMatch(r -> r.getAlerta().isEmpty()));
    }

    @Test
    @DisplayName("Com só 3 leituras (mesmo altas) o sistema se cala e não grava alerta")
    void naoDeveAlertarComHistoricoInsuficiente() {
        // Arrange
        List<Medicao> historico = HistoricoFake.de("PLV-01", TipoSensor.PLUVIOMETRO, 80, 85, 90);
        when(medicaoDAO.buscarUltimasPorSensor(eq("PLV-01"), anyInt())).thenReturn(historico);

        // Act
        List<ResultadoPrevisao> resultados = servico.preverEAlertar(pluviometro);

        // Assert
        verify(alertaDAO, never()).salvar(any());
        assertTrue(resultados.stream().noneMatch(r -> r.getPrevisao().isConfiavel()));
    }

    @Test
    @DisplayName("Leitura válida é gravada no banco com o valor lido")
    void deveGravarLeituraValida() throws Exception {
        // Arrange
        Sensor sensor = mock(Sensor.class);
        when(sensor.lerValor()).thenReturn(12.5);
        when(sensor.getId()).thenReturn("PLV-09");
        when(sensor.getTipo()).thenReturn(TipoSensor.PLUVIOMETRO);

        // Act
        servico.lerEGravar(sensor);

        // Assert
        ArgumentCaptor<Medicao> captor = ArgumentCaptor.forClass(Medicao.class);
        verify(medicaoDAO).salvar(captor.capture());
        assertEquals(12.5, captor.getValue().getValor(), 0.0001);
        assertEquals("PLV-09", captor.getValue().getSensorId());
    }

    @Test
    @DisplayName("Sensor fora do ar: FonteIndisponivelException e NADA é gravado")
    void naoDeveGravarQuandoSensorFalha() throws Exception {
        // Arrange
        Sensor sensor = mock(Sensor.class);
        when(sensor.lerValor()).thenThrow(new FonteIndisponivelException("sem sinal"));

        // Act
        assertThrows(FonteIndisponivelException.class, () -> servico.lerEGravar(sensor));

        // Assert
        verify(medicaoDAO, never()).salvar(any());
    }

    @Test
    @DisplayName("Leitura impossível (-5 mm/h): DadosInvalidosException e NADA é gravado")
    void naoDeveGravarLeituraInvalida() throws Exception {
        // Arrange
        Sensor sensor = mock(Sensor.class);
        when(sensor.lerValor()).thenReturn(-5.0);
        when(sensor.getId()).thenReturn("PLV-09");
        when(sensor.getTipo()).thenReturn(TipoSensor.PLUVIOMETRO);

        // Act
        assertThrows(DadosInvalidosException.class, () -> servico.lerEGravar(sensor));

        // Assert
        verify(medicaoDAO, never()).salvar(any());
    }
}
