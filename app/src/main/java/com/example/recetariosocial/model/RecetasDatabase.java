package com.example.recetariosocial.model;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
/*
@Database(entities = {Perfil.class}, version = 1, exportSchema = false)
public abstract class RecetasDatabase extends RoomDatabase {

    private static RecetasDatabase INSTANCE;

    public abstract UsuarioDao usuarioDao();

    public static synchronized RecetasDatabase getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    RecetasDatabase.class,
                     "recetas_database"
            ).allowMainThreadQueries().build();
        }

        return INSTANCE;
    }


}
*/