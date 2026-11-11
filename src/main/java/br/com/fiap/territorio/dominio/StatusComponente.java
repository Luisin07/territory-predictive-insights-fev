package br.com.fiap.territorio.dominio;

public enum StatusComponente {
    OPERACIONAL,   // todos os sensores respondendo
    DEGRADADO,     // parte dos sensores fora do ar
    INOPERANTE     // nenhum sensor respondendo: o ponto está "cego"
}
