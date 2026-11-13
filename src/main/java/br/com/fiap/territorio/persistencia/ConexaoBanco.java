package br.com.fiap.territorio.persistencia;

import br.com.fiap.territorio.excecao.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre conexões com o H2. Por padrão o banco é um ARQUIVO em ./dados/,
 * então o histórico continua lá depois que o programa fecha.
 * A URL pode ser trocada com -Ddb.url=... (os testes usam um banco em memória).
 */
public class ConexaoBanco {

    public static final String URL_PADRAO = "jdbc:h2:file:./dados/territorio";

    private final String url;

    public ConexaoBanco() {
        this(System.getProperty("db.url", URL_PADRAO));
    }

    public ConexaoBanco(String url) {
        this.url = url;
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    /**
     * Cria as tabelas se ainda não existirem, executando src/main/resources/schema.sql.
     * O script é fixo (vem do próprio projeto, não do usuário), por isso um Statement simples basta aqui.
     */
    public void inicializarEsquema() {
        String script = lerScript("/schema.sql");
        try (Connection conexao = abrir(); Statement stmt = conexao.createStatement()) {
            for (String comando : script.split(";")) {
                if (!comando.isBlank()) {
                    stmt.execute(comando);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível criar as tabelas do banco (" + url + ")", e);
        }
    }

    private String lerScript(String recurso) {
        try (InputStream in = ConexaoBanco.class.getResourceAsStream(recurso)) {
            if (in == null) {
                throw new PersistenciaException("Script " + recurso + " não encontrado no classpath",
                        new IOException("recurso ausente"));
            }
            StringBuilder sb = new StringBuilder();
            for (String linha : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (!linha.trim().startsWith("--")) {   // ignora comentários SQL
                    sb.append(linha).append('\n');
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new PersistenciaException("Falha ao ler o script " + recurso, e);
        }
    }

    public String getUrl() {
        return url;
    }
}
