package br.com.fiap.territorio.servico;

/** Produto final do RelatorioBuilder: um texto pronto para exibir. */
public class Relatorio {

    private final String conteudo;

    Relatorio(String conteudo) {   // só o builder (mesmo pacote) cria
        this.conteudo = conteudo;
    }

    public String getConteudo() {
        return conteudo;
    }

    @Override
    public String toString() {
        return conteudo;
    }
}
