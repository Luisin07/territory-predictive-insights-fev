package br.com.fiap.territorio.dominio;

import java.util.Random;

/** Mede intensidade de chuva em mm/h. */
public class Pluviometro extends SensorSimulado {

    public Pluviometro(String id, Random random) {
        super(id, 8.0, random);
    }

    @Override
    public TipoSensor getTipo() { return TipoSensor.PLUVIOMETRO; }

    @Override
    protected double variacaoMaxima() { return 9.0; }

    @Override
    protected double minimoRealista() { return 0.0; }

    @Override
    protected double maximoRealista() { return 90.0; }
}
