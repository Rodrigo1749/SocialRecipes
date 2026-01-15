package com.example.recetariosocial.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Comentario;
import com.example.recetariosocial.model.Estado;
import com.example.recetariosocial.model.Favorito;
import com.example.recetariosocial.model.Ingrediente;
import com.example.recetariosocial.model.Pais;
import com.example.recetariosocial.model.Paso;
import com.example.recetariosocial.model.Perfil;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallesRecetaActivity extends AppCompatActivity {

    private ImageView ivReceta, ivAutor;
    private TextView tvTitulo, tvDescripcion, tvAutor, tvLocation;
    private RecyclerView rvIngredientes, rvPasos, rvComentarios;
    private Button btnEditarReceta, btnEliminarReceta, btnFavoritos, btnEnviarComentario;
    private EditText etComentario;

    private ApiService apiService;
    private int recetaId = -1;
    private int currentUserId = -1;
    private boolean isFavorito = false;
    private ComentarioAdapter comentarioAdapter;
    private Receta recetaActual;


    // Método principal que se llama al crear la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_receta);

        apiService = SupabaseClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        initViews();
        recetaId = getIntent().getIntExtra("recetaId", -1);

        if (recetaId != -1) {
            loadRecetaDetails(recetaId);
        } else {
            Toast.makeText(this, "Error al cargar receta", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Inicializa las vistas (componentes de la UI) del layout.
    private void initViews() {
        ivReceta = findViewById(R.id.ivReceta);
        ivAutor = findViewById(R.id.ivAutor);
        tvTitulo = findViewById(R.id.tvTituloDeLaReceta);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvAutor = findViewById(R.id.textViewAuthor);
        tvLocation = findViewById(R.id.textViewLocation);
        rvIngredientes = findViewById(R.id.rvIngredientes);
        rvPasos = findViewById(R.id.rvPasos);
        rvComentarios = findViewById(R.id.rvComentarios);
        btnEditarReceta = findViewById(R.id.btnEditarReceta);
        btnEliminarReceta = findViewById(R.id.btnEliminarReceta);
        btnFavoritos = findViewById(R.id.btnFavoritos);
        etComentario = findViewById(R.id.etComentario);
        btnEnviarComentario = findViewById(R.id.btnAgregarComentario);

        btnEnviarComentario.setOnClickListener(v -> enviarComentario());
        btnFavoritos.setOnClickListener(v -> toggleFavorito());
    }

    // Carga los detalles completos de la receta desde la API usando su ID.
    private void loadRecetaDetails(int id) {

        apiService.getRecetaById("eq." + id).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    recetaActual = response.body().get(0);
                    populateReceta(recetaActual);
                    checkIfFavorito();
                } else {
                    Toast.makeText(DetallesRecetaActivity.this, "Receta no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Toast.makeText(DetallesRecetaActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Rellena la UI con los datos de la receta una vez cargados.
    private void populateReceta(Receta receta) {
        tvTitulo.setText(receta.Titulo);
        tvDescripcion.setText(receta.Descripcion);

        if (receta.ImagenUrl != null && !receta.ImagenUrl.isEmpty()) {
            Glide.with(this).load(receta.ImagenUrl).into(ivReceta);
        }

        // Muestra botones de edición/eliminación si el usuario actual es el autor.
        if (currentUserId != -1 && receta.IdUsuario == currentUserId) {
            btnEditarReceta.setVisibility(View.VISIBLE);
            btnEliminarReceta.setVisibility(View.VISIBLE);

            btnEditarReceta.setOnClickListener(v -> {
                Intent intent = new Intent(DetallesRecetaActivity.this, RegistraReceta.class);
                intent.putExtra("EDIT_RECIPE_ID", receta.IdReceta);
                startActivity(intent);
            });

            btnEliminarReceta.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        loadAutor(receta.IdUsuario);
        if (receta.IdEstado > 0) loadUbicacion(receta.IdEstado);
        loadIngredientes(receta.IdReceta);
        loadPasos(receta.IdReceta);
        loadComentarios(receta.IdReceta);
    }

    // Muestra un diálogo de confirmación antes de eliminar la receta.
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Receta")
                .setMessage("¿Estás seguro de que quieres eliminar esta receta? Esta acción es irreversible.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteRecipe())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Llama a la API para eliminar la receta actual.
    private void deleteRecipe() {
        if (recetaId == -1) {
            Toast.makeText(this, "Error: No se puede identificar la receta.", Toast.LENGTH_SHORT).show();
            return;
        }

        String idQuery = "eq." + recetaId;
        apiService.deleteReceta(idQuery).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetallesRecetaActivity.this, "Receta eliminada correctamente", Toast.LENGTH_LONG).show();
                    finish(); // Volver a la pantalla anterior
                } else {
                    Toast.makeText(DetallesRecetaActivity.this, "Error al eliminar la receta. Código: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetallesRecetaActivity.this, "Fallo de red al intentar eliminar la receta.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Comprueba si la receta actual está en la lista de favoritos del usuario.
    private void checkIfFavorito() {
        if (currentUserId == -1) return;

        apiService.getFavoritosByUser("eq." + currentUserId).enqueue(new Callback<List<Favorito>>() {
            @Override
            public void onResponse(Call<List<Favorito>> call, Response<List<Favorito>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFavorito = false;
                    for (Favorito fav : response.body()) {
                        if (fav.idReceta == recetaId) {
                            isFavorito = true;
                            break;
                        }
                    }
                    updateFavoritoButton();
                }
            }

            @Override
            public void onFailure(Call<List<Favorito>> call, Throwable t) {
                // No se muestra error al usuario, simplemente el botón no se actualiza.
            }
        });
    }

    // Gestiona la acción de pulsar el botón de favorito (añadir o quitar).
    private void toggleFavorito() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Inicia sesión para añadir a favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFavoritos.setEnabled(false); // Desactivar para evitar clics múltiples

        if (isFavorito) {
            removeFavorito();
        } else {
            addFavorito();
        }
    }

    // Llama a la API para añadir la receta a la lista de favoritos.
    private void addFavorito() {
        Favorito favorito = new Favorito(currentUserId, recetaId);
        apiService.addFavorito(favorito).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isFavorito = true;
                    updateFavoritoButton();
                    Toast.makeText(DetallesRecetaActivity.this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetallesRecetaActivity.this, "Error al añadir a favoritos", Toast.LENGTH_SHORT).show();
                }
                btnFavoritos.setEnabled(true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetallesRecetaActivity.this, "Fallo de red", Toast.LENGTH_SHORT).show();
                btnFavoritos.setEnabled(true);
            }
        });
    }

    // Llama a la API para quitar la receta de la lista de favoritos.
    private void removeFavorito() {
        apiService.removeFavorito("eq." + currentUserId, "eq." + recetaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isFavorito = false;
                    updateFavoritoButton();
                    Toast.makeText(DetallesRecetaActivity.this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetallesRecetaActivity.this, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show();
                }
                btnFavoritos.setEnabled(true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetallesRecetaActivity.this, "Fallo de red", Toast.LENGTH_SHORT).show();
                btnFavoritos.setEnabled(true);
            }
        });
    }

    // Actualiza el texto del botón de favoritos según el estado actual.
    private void updateFavoritoButton() {
        if (isFavorito) {
            btnFavoritos.setText("Quitar de Favoritos");
        } else {
            btnFavoritos.setText("Agregar a Favoritos");
        }
    }


    // Carga y muestra la información del autor de la receta.
     private void loadAutor(int userId) {
        apiService.getPerfilByUserId("eq." + userId).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Perfil perfil = response.body().get(0);
                    tvAutor.setText("Por: " + perfil.nombreDeUsuario);
                    ivAutor.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            }
            @Override
            public void onFailure(Call<List<Perfil>> call, Throwable t) {}
        });
    }

    // Carga y muestra la ubicación (Estado y País) de la receta.
    private void loadUbicacion(int estadoId) {
        apiService.getEstadoById("eq." + estadoId).enqueue(new Callback<List<Estado>>() {
            @Override
            public void onResponse(Call<List<Estado>> call, Response<List<Estado>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Estado estado = response.body().get(0);
                    apiService.getPaisById("eq." + estado.idPais).enqueue(new Callback<List<Pais>>() {
                        @Override
                        public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                            String pais = (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) 
                                    ? response.body().get(0).nombre : "";
                            tvLocation.setText(estado.nombre + (pais.isEmpty() ? "" : ", " + pais));
                        }
                        @Override public void onFailure(Call<List<Pais>> call, Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(Call<List<Estado>> call, Throwable t) {}
        });
    }

    // Carga y muestra la lista de ingredientes de la receta.
    private void loadIngredientes(int recetaId) {
        apiService.getIngredientesByReceta("eq." + recetaId).enqueue(new Callback<List<Ingrediente>>() {
            @Override
            public void onResponse(Call<List<Ingrediente>> call, Response<List<Ingrediente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IngredienteAdapter ingAdapter = new IngredienteAdapter(false);
                    rvIngredientes.setLayoutManager(new LinearLayoutManager(DetallesRecetaActivity.this));
                    rvIngredientes.setAdapter(ingAdapter);
                    ingAdapter.setIngredientes(response.body());
                }
            }
            @Override public void onFailure(Call<List<Ingrediente>> call, Throwable t) {}
        });
    }

    // Carga y muestra la lista de pasos de la receta.
    private void loadPasos(int recetaId) {
        apiService.getPasosByReceta("eq." + recetaId).enqueue(new Callback<List<Paso>>() {
            @Override
            public void onResponse(Call<List<Paso>> call, Response<List<Paso>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PasoAdapter pasAdapter = new PasoAdapter(false);
                    rvPasos.setLayoutManager(new LinearLayoutManager(DetallesRecetaActivity.this));
                    rvPasos.setAdapter(pasAdapter);
                    pasAdapter.setPasos(response.body());
                }
            }
            @Override public void onFailure(Call<List<Paso>> call, Throwable t) {}
        });
    }

    // Carga y muestra la lista de comentarios de la receta.
    private void loadComentarios(int recetaId) {
        apiService.getComentariosByReceta("eq." + recetaId).enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comentarioAdapter = new ComentarioAdapter();
                    comentarioAdapter.setComentarios(response.body());
                    rvComentarios.setLayoutManager(new LinearLayoutManager(DetallesRecetaActivity.this));
                    rvComentarios.setAdapter(comentarioAdapter);
                }
            }
            @Override public void onFailure(Call<List<Comentario>> call, Throwable t) {}
        });
    }

    // Envía un nuevo comentario a la API.
    private void enviarComentario() {
        String texto = etComentario.getText().toString().trim();
        if (texto.isEmpty()) return;

        if (currentUserId == -1) {
            Toast.makeText(this, "Inicia sesión para comentar", Toast.LENGTH_SHORT).show();
            return;
        }

        Comentario comentario = new Comentario(recetaId, currentUserId, texto);
        apiService.createComentario(comentario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    etComentario.setText("");
                    Toast.makeText(DetallesRecetaActivity.this, "Comentario enviado", Toast.LENGTH_SHORT).show();
                    loadComentarios(recetaId);
                } else {
                    Toast.makeText(DetallesRecetaActivity.this, "Error al enviar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetallesRecetaActivity.this, "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
