package com.example.recetariosocial.model;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName(value = "idUsuario", alternate = {"id"})
    public Integer idUsuario;
    
    @SerializedName("correo")
    public String correo;
    
    @SerializedName("contraseña")
    public String contraseña;

    @SerializedName("nombre")
    public String nombre;
}