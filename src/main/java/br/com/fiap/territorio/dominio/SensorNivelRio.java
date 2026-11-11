package br.com.fiap.territorio.dominio;

import java.util.Random;

/** Mede o nível da água do córrego em metros. */
public class SensorNivelRio extends SensorSimulado {

    public SensorNivelRio(String id, Random random) {
        super(id, 2.0, random);
    }

    @Override
    public TipoSensor getTipo() { return TipoSensor.NIVEL_RIO; }

    @Override
    protected double variacaoMaxima() { return 0.5; }

    @Override
    protected double minimoRealista() { return 0.5; }

    @Override
    protected double maximoRealista() { return 8.0; }
}
