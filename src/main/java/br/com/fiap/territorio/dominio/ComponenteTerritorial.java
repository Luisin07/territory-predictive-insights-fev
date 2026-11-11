package br.com.fiap.territorio.dominio;

/**
 * Qualquer coisa do território que é monitorada e tem um estado.
 * É abstrata porque "componente territorial" sozinho não existe: existe
 * um ponto de monitoramento, uma área de risco etc. Cada filho decide
 * COMO verificar o próprio status.
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private final String nome;
    private final String regiao;
    private StatusComponente status;

    protected ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
        this.status = StatusComponente.OPERACIONAL;
    }

    /** Recalcula e devolve o status atual do componente. */
    public abstract StatusComponente verificarStatus();

    protected void setStatus(StatusComponente status) {
        this.status = status;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getRegiao() { return regiao; }
    public StatusComponente getStatus() { return status; }
}
