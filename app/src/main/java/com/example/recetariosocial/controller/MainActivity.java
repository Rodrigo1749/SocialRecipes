package com.example.recetariosocial.controller;

import androidx.core.graphics.Insets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recetariosocial.R;

public class MainActivity extends AppCompatActivity {


    private Button btnInicioSesion, btnRegistrarse;

    // Método principal que se llama al crear la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        // Inicializa los botones de la pantalla de bienvenida
        btnInicioSesion = findViewById(R.id.btnInicioSesion);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);

        // Configura el listener para navegar a la pantalla de Login
        btnInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
           });

        // Configura el listener para navegar a la pantalla de Registro
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, RegistrarseActivity.class);
                startActivity(intent);
            }
        });

        // Ajusta el padding de la vista para que no se solape con las barras del sistema (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Inicio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
