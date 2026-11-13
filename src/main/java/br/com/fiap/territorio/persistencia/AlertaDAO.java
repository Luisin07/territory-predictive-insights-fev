package br.com.fiap.territorio.persistencia;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela ALERTA, no mesmo padrão do MedicaoDAO. */
public class AlertaDAO {

    private static final String INSERIR =
            "INSERT INTO alerta (sensor_id, tipo, nivel, valor_previsto, limite_estourado, estrategia, data_hora) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String TODOS =
            "SELECT id, sensor_id, tipo, nivel, valor_previsto, limite_estourado, estrategia, data_hora "
                    + "FROM alerta ORDER BY data_hora, id";
    private static final String RECENTES =
            "SELECT id, sensor_id, tipo, nivel, valor_previsto, limite_estourado, estrategia, data_hora "
                    + "FROM alerta ORDER BY data_hora DESC, id DESC LIMIT ?";

    private final ConexaoBanco conexaoBanco;

    public AlertaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco;
    }

    public void salvar(Alerta alerta) {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alerta.getSensorId());
            ps.setString(2, alerta.getTipo().name());
            ps.setString(3, alerta.getNivel().name());
            ps.setDouble(4, alerta.getValorPrevisto());
            ps.setDouble(5, alerta.getLimiteEstourado());
            ps.setString(6, alerta.getEstrategia());
            ps.setTimestamp(7, Timestamp.valueOf(alerta.getDataHora()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    alerta.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao gravar alerta do sensor " + alerta.getSensorId(), e);
        }
    }

    public List<Alerta> listarTodos() {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(TODOS)) {
            return lerLista(ps);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar alertas", e);
        }
    }

    public List<Alerta> listarRecentes(int limite) {
        try (Connection con = conexaoBanco.abrir();
             PreparedStatement ps = con.prepareStatement(RECENTES)) {
            ps.setInt(1, limite);
            return lerLista(ps);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar alertas recentes", e);
        }
    }

    private List<Alerta> lerLista(PreparedStatement ps) throws SQLException {
        List<Alerta> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Alerta(
                        rs.getLong("id"),
                        rs.getString("sensor_id"),
                        TipoSensor.valueOf(rs.getString("tipo")),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        rs.getDouble("valor_previsto"),
                        rs.getDouble("limite_estourado"),
                        rs.getString("estrategia"),
                        rs.getTimestamp("data_hora").toLocalDateTime()));
            }
        }
        return lista;
    }
}
