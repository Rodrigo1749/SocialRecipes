package com.example.recetariosocial.model;

public class Comentario {


    public Integer idComentario;
    
    public int idReceta;
    public int idUsuario;
    public String texto;

    public Comentario(int idReceta, int idUsuario, String texto) {
        this.idReceta = idReceta;
        this.idUsuario = idUsuario;
        this.texto = texto;
    }

    public Comentario() {}
}