package com.example.recetariosocial.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

    @Dao
    public interface PerfilDao {

        @Insert
        void insert(Perfil perfil);

        @Update
        void update(Perfil perfil);

        @Delete
        void delete(Perfil perfil);

        @Query("SELECT * FROM perfiles")
        List<Perfil> getAllPerfiles();

    }
