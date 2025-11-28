package com.example.recetariosocial.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Recetas")
public class Receta {

    @PrimaryKey(autoGenerate = true)
    public int IdReceta;

    //ForeignKey
    public int  IdUsuario;

    @ColumnInfo(name = "Titulo")
    public String Titulo;

    @ColumnInfo(name = "Descripcion")
    public String Descripcion;

    @ColumnInfo(name = "ImagenUrl")
    public String ImagenUrl;

    //ForeignKey
    public int IdEstado;


}
