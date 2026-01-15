package com.example.recetariosocial.model;

public class Ingrediente {
    public Integer idIngrediente;
    public Integer idReceta;
    public String nombre;
    public String cantidad;

    public Ingrediente(String nombre, String cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public Ingrediente() {}
}