package com.example.recetariosocial.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvHomeRecetas;
    private FloatingActionButton fabAddRecipe;
    private ImageButton btnSearch, btnProfile, btnFavorites; // Se añade el nuevo botón
    private RecetaAdapter adapter;
    private ApiService apiService;

    // Método principal que se llama al crear la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        apiService = SupabaseClient.getClient().create(ApiService.class);

        initViews();
        setupRecyclerView();
        setupClickListeners();
    }

    // Se llama cuando la actividad vuelve al primer plano, asegurando que los datos estén actualizados
    @Override
    protected void onResume() {
        super.onResume();
        loadRecetas();
    }

    // Inicializa las vistas (componentes de la UI) del layout
    private void initViews() {
        rvHomeRecetas = findViewById(R.id.rvHomeRecetas);
        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        btnSearch = findViewById(R.id.btnSearch);
        btnProfile = findViewById(R.id.btnProfile);
        btnFavorites = findViewById(R.id.btnFavorites); // Se inicializa el nuevo botón
    }

    // Configura el RecyclerView, su adaptador y el listener para los clics en cada receta
    private void setupRecyclerView() {
        rvHomeRecetas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecetaAdapter();
        rvHomeRecetas.setAdapter(adapter);

        adapter.setOnItemClickListener(receta -> {
            Intent intent = new Intent(HomeActivity.this, DetallesRecetaActivity.class);
            intent.putExtra("recetaId", receta.IdReceta);
            startActivity(intent);
        });
    }

    // Asigna las acciones a los botones de la pantalla (Añadir, Buscar, Perfil, Favoritos)
    private void setupClickListeners() {
        fabAddRecipe.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, RegistraReceta.class));
        });

        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, BuscadorActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);
            
            if (userId != -1) {
                Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomeActivity.this, "Por favor inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Listener para el botón de favoritos
        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, FavoritosActivity.class));
        });
    }

    // Carga la lista de todas las recetas desde la API y las muestra en el RecyclerView
    private void loadRecetas() {
        apiService.getRecetas().enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setRecetas(response.body());
                } else {
                    Toast.makeText(HomeActivity.this, "Error al cargar recetas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
