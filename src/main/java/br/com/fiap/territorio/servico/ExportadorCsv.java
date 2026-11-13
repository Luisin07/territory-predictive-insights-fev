package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Medicao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * BÔNUS: exporta o histórico consultado para CSV.
 * Usa ";" como separador e vírgula decimal para abrir direto no Excel em português.
 */
public class ExportadorCsv {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public Path exportar(List<Medicao> medicoes, Path destino) throws IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            writer.write("id;sensor;tipo;valor;unidade;data_hora");
            writer.newLine();
            for (Medicao m : medicoes) {
                writer.write(String.format(PT_BR, "%d;%s;%s;%.2f;%s;%s",
                        m.getId(), m.getSensorId(), m.getTipo().name(), m.getValor(),
                        m.getTipo().getUnidade(), m.getDataHora().format(FORMATO)));
                writer.newLine();
            }
        }
        return destino;
    }
}
