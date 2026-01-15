package com.example.recetariosocial.model;

import com.google.gson.annotations.SerializedName;

public class Perfil {

    @SerializedName("idPerfil")
    public Integer idPerfil;
    @SerializedName("idUsuario")
    public Integer idUsuario;
    // Se envía "nombre_de_usuario" para coincidir con la BD
    @SerializedName("nombre_de_usuario")
    public String nombreDeUsuario;

    @SerializedName("nombre")
    public String nombre;
    public String correo;
    public String contraseña;
    public String biografia;
}
