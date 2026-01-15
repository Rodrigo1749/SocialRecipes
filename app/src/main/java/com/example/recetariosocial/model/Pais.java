package com.example.recetariosocial.model;

public class Pais {
    public int idPais;
    public String nombre;

    public Pais(String nombre) {
        this.nombre = nombre;
    }
    
    public Pais() {}
    
    @Override
    public String toString() {
        return nombre;
    }
}