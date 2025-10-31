package com.example.simcra2.Model;

import java.io.Serializable;

public class Alerta implements Serializable {
    private String fechaHoraOriginal;
    private String titular;
    private String resumen;
    private String contenidoExtendido;

    private static int contadorGlobal = 0;
    private final int id;

    public Alerta(String fecha, String titular, String resumen, String contenido) {
        this.fechaHoraOriginal = fecha;
        this.titular = titular;
        this.resumen = resumen;
        this.contenidoExtendido = contenido;
        this.id = contadorGlobal++;
    }

    public int getId() {
        return id;
    }


    public String getFechaHoraOriginal() { return fechaHoraOriginal; }
    public String getTitular() { return titular; }
    public String getResumen() { return resumen; }
    public String getContenidoExtendido() { return contenidoExtendido; }

}