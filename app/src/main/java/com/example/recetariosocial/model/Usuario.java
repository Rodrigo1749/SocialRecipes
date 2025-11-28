package com.example.recetariosocial.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Usuarios")
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    public int idUsuario;

    @ColumnInfo(name = "correo")
    public String correo;

    @ColumnInfo(name = "contraseña")
    public String contraseña;

}
