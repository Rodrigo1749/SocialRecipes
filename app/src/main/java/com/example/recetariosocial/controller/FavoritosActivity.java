package com.example.recetariosocial.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Favorito;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritosActivity extends AppCompatActivity {

    private RecyclerView rvFavoritosRecetas;
    private RecetaAdapter adapter;
    private ApiService apiService;
    private int currentUserId = -1;

    // Método principal que se llama al crear la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        Toolbar toolbar = findViewById(R.id.toolbar_favoritos);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiService = SupabaseClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        initRecyclerView();

        if (currentUserId == -1) {
            Toast.makeText(this, "Error de sesión, por favor inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Se llama cuando la actividad vuelve a ser visible
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != -1) {
            loadFavoritosIds();
        }
    }

    // Configura el RecyclerView, su adaptador y el listener para los clics en cada receta
    private void initRecyclerView() {
        rvFavoritosRecetas = findViewById(R.id.rvFavoritosRecetas);
        rvFavoritosRecetas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecetaAdapter();
        rvFavoritosRecetas.setAdapter(adapter);

        adapter.setOnItemClickListener(receta -> {
            Intent intent = new Intent(FavoritosActivity.this, DetallesRecetaActivity.class);
            intent.putExtra("recetaId", receta.IdReceta);
            startActivity(intent);
        });
    }

    // Obtiene los IDs de las recetas favoritas del usuario desde la API
    private void loadFavoritosIds() {
        apiService.getFavoritosByUser("eq." + currentUserId).enqueue(new Callback<List<Favorito>>() {
            @Override
            public void onResponse(Call<List<Favorito>> call, Response<List<Favorito>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(FavoritosActivity.this, "No tienes recetas favoritas.", Toast.LENGTH_LONG).show();
                        adapter.setRecetas(new ArrayList<>());
                    } else {
                        // Extrae los IDs y los formatea para la siguiente llamada a la API
                        List<Integer> recetaIds = response.body().stream()
                                .map(fav -> fav.idReceta)
                                .collect(Collectors.toList());

                        String formattedIds = "in." + recetaIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",", "(", ")"));

                        loadRecetasFavoritas(formattedIds);
                    }
                } else {
                    Toast.makeText(FavoritosActivity.this, "Error al cargar favoritos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Favorito>> call, Throwable t) {
                Toast.makeText(FavoritosActivity.this, "Error de conexión.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Carga los detalles completos de las recetas favoritas usando la lista de IDs
    private void loadRecetasFavoritas(String ids) {
        apiService.getRecetasByIds(ids).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setRecetas(response.body());
                } else {
                    Toast.makeText(FavoritosActivity.this, "Error al cargar los detalles de las recetas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Toast.makeText(FavoritosActivity.this, "Error de conexión.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gestiona la acción del botón de 'atrás' en la barra de herramientas
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
