package com.example.recetariosocial.model;

public class Estado {
    public int idEstado;
    public int idPais;
    public String nombre;

    public Estado(int idPais, String nombre) {
        this.idPais = idPais;
        this.nombre = nombre;
    }
    
    public Estado() {}

    @Override
    public String toString() {
        return nombre;
    }
}