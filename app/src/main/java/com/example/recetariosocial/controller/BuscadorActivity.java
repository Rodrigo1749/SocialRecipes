package com.example.recetariosocial.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscadorActivity extends AppCompatActivity {

    private EditText etBuscador;
    private RecyclerView recyclerViewRecipes;
    private RecetaAdapter adapter;
    private ApiService apiService;

    // Método principal que se llama al crear la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador);

        // Inicializar cliente de Supabase
        apiService = SupabaseClient.getClient().create(ApiService.class);

        initViews();
        setupRecyclerView();
        setupSearchListener();
    }

    // Inicializa las vistas (componentes de la UI) del layout.
    private void initViews() {
        etBuscador = findViewById(R.id.etBuscador);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
    }

    // Configura el RecyclerView, su adaptador y el listener para los clics.
    private void setupRecyclerView() {
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecetaAdapter();
        recyclerViewRecipes.setAdapter(adapter);

        adapter.setOnItemClickListener(receta -> {
            Intent intent = new Intent(BuscadorActivity.this, DetallesRecetaActivity.class);
            intent.putExtra("recetaId", receta.IdReceta);
            startActivity(intent);
        });
        
        // Carga todas las recetas inicialmente al abrir la pantalla.
        loadRecetas("");
    }

    // Configura el listener que escucha los cambios de texto en el buscador.
    private void setupSearchListener() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Llama a la función de carga con el nuevo texto para filtrar en tiempo real.
                loadRecetas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Carga las recetas desde la API, aplicando un filtro de búsqueda si se proporciona.
    private void loadRecetas(String query) {
        Call<List<Receta>> call;
        
        // Si no hay consulta, obtiene todas las recetas; si la hay, busca con el filtro.
        if (query.isEmpty()) {
            call = apiService.getRecetas(); 
        } else {
            call = apiService.searchRecetas("ilike.*" + query + "*");
        }
        
        call.enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setRecetas(response.body());
                } else {
                    adapter.setRecetas(new ArrayList<>()); 
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Toast.makeText(BuscadorActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
