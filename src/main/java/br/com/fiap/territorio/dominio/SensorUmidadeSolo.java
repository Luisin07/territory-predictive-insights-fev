package br.com.fiap.territorio.dominio;

import java.util.Random;

/** Mede a saturação do solo em %. Solo saturado não absorve mais chuva e escorre para as ruas. */
public class SensorUmidadeSolo extends SensorSimulado {

    public SensorUmidadeSolo(String id, Random random) {
        super(id, 60.0, random);
    }

    @Override
    public TipoSensor getTipo() { return TipoSensor.UMIDADE_SOLO; }

    @Override
    protected double variacaoMaxima() { return 6.0; }

    @Override
    protected double minimoRealista() { return 20.0; }

    @Override
    protected double maximoRealista() { return 100.0; }
}
