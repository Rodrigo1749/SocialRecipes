package com.example.recetariosocial.model;

public class Paso {
    public Integer idPaso;
    public Integer idReceta;
    public Integer numeroPaso;
    public String descripcion;

    // Constructor
    public Paso(String descripcion) {
        this.descripcion = descripcion;
    }

    public Paso() {}
}