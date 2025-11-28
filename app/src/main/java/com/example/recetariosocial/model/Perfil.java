package com.example.recetariosocial.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


@Entity(tableName = "Perfiles")
public class Perfil {

        @PrimaryKey(autoGenerate = true)
        public int idPerfil;

       // @ForeignKey()
        public int idUsuario;

        @ColumnInfo(name = "nombreDeUsuario")
        public String nombreDeUsuario;
        @ColumnInfo(name = "correo")
        public String correo;

        @ColumnInfo(name = "contraseña")
        public String contraseña;

        @ColumnInfo(name = "biografia")
        public String biografia;

    }

