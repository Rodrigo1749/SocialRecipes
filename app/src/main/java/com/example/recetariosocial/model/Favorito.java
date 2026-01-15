package com.example.recetariosocial.model;

import com.google.gson.annotations.SerializedName;

public class Favorito {

    @SerializedName("idUsuario")
    public int idUsuario;

    @SerializedName("idReceta")
    public int idReceta;


    public Favorito(int idUsuario, int idReceta) {
        this.idUsuario = idUsuario;
        this.idReceta = idReceta;
    }
}
