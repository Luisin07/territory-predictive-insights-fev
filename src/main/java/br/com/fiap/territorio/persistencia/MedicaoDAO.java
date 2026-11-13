package br.com.fiap.territorio.persistencia;

import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela MEDICAO. Toda consulta usa PreparedStatement com "?"
 * (nunca concatenação de String) e todo recurso é fechado por try-with-resources.
 */
public class MedicaoDAO {

    private static final String INSERIR =
            "INSERT INTO medicao (sensor_id, tipo, valor, data_hora) VALUES (?, ?, ?, ?)";
    private static final String POR_SENSOR =
            "SELECT id, sensor_id, tipo, valor, data_hora FROM medicao WHERE sensor_id = ? ORDER BY data_hora, id";
    private static final String ULTIMAS_POR_SENSOR =
            "SELECT id, sensor_id, tipo, valor, data_hora FROM medicao WHERE sensor_id = ? "
                    + "ORDER BY data_hora DESC, id DESC LIMIT ?";
    private static final String CONTAR = "SELECT COUNT(*) FROM medicao";

    private final ConexaoBanco conexaoBanco;

    public MedicaoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco;
    }

    public void salvar(Medicao medicao) {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicao.getSensorId());
            ps.setString(2, medicao.getTipo().name());
            ps.setDouble(3, medicao.getValor());
            ps.setTimestamp(4, Timestamp.valueOf(medicao.getDataHora()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    medicao.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao gravar medição do sensor " + medicao.getSensorId(), e);
        }
    }

    /** Histórico completo de um sensor, do mais antigo para o mais recente. */
    public List<Medicao> buscarPorSensor(String sensorId) {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(POR_SENSOR)) {
            ps.setString(1, sensorId);
            return lerLista(ps);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar histórico do sensor " + sensorId, e);
        }
    }

    /**
     * As N leituras mais recentes, devolvidas em ordem CRONOLÓGICA
     * (é o formato que as estratégias de previsão esperam).
     */
    public List<Medicao> buscarUltimasPorSensor(String sensorId, int limite) {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(ULTIMAS_POR_SENSOR)) {
            ps.setString(1, sensorId);
            ps.setInt(2, limite);
            List<Medicao> recentesPrimeiro = lerLista(ps);
            Collections.reverse(recentesPrimeiro);
            return recentesPrimeiro;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar últimas leituras do sensor " + sensorId, e);
        }
    }

    public Optional<Medicao> buscarUltimaPorSensor(String sensorId) {
        List<Medicao> ultima = buscarUltimasPorSensor(sensorId, 1);
        return ultima.isEmpty() ? Optional.empty() : Optional.of(ultima.get(0));
    }

    public long contar() {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(CONTAR);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao contar medições", e);
        }
    }

    private List<Medicao> lerLista(PreparedStatement ps) throws SQLException {
        List<Medicao> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Medicao(
                        rs.getLong("id"),
                        rs.getString("sensor_id"),
                        TipoSensor.valueOf(rs.getString("tipo")),
                        rs.getDouble("valor"),
                        rs.getTimestamp("data_hora").toLocalDateTime()));
            }
        }
        return lista;
    }
}
