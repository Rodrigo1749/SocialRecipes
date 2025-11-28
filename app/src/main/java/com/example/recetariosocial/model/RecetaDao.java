package com.example.recetariosocial.model;

import static android.icu.text.MessagePattern.ArgType.SELECT;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.util.TableInfo;

import java.nio.channels.SelectableChannel;
import java.util.List;

@Dao
public interface RecetaDao {

    @Insert
    void insert(Receta receta);

    @Update
    void update(Receta receta);

    @Delete
    void delete(Receta receta);

    @Query("SELECT * FROM recetas")
    List<Receta>getAllRecetas();

}
