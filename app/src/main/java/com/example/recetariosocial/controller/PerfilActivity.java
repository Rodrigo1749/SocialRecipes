package com.example.recetariosocial.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Perfil;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private EditText etNombre, etBio;
    private TextView tvEmail;
    private Button btnActualizar, btnEliminar;
    private ApiService apiService;
    private int loggedInUserId;
    private int targetUserId; // El ID del perfil que queremos ver
    private Perfil currentPerfil;
    private boolean isMyProfile;

    // Método principal que se llama al crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        apiService = SupabaseClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", Context.MODE_PRIVATE);
        loggedInUserId = prefs.getInt("userId", -1);

        if (loggedInUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        targetUserId = getIntent().getIntExtra("targetUserId", loggedInUserId);
        isMyProfile = (loggedInUserId == targetUserId);

        initViews();
        setupClickListeners();
        loadUserData();
    }

    // Inicializa las vistas y ajusta su estado según si es el perfil propio o de otro usuario
    private void initViews() {
        etNombre = findViewById(R.id.etNombreDeUsuario);
        etBio = findViewById(R.id.etBiografia);
        tvEmail = findViewById(R.id.tvEmail);
        btnActualizar = findViewById(R.id.btnAcualizarPerfil);
        btnEliminar = findViewById(R.id.btnEliminarCuenta);

        if (!isMyProfile) {
            etNombre.setEnabled(false);
            etBio.setEnabled(false);
            btnActualizar.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        }
    }

    // Carga los datos del perfil del usuario (nombre, correo, bio) desde la API
    private void loadUserData() {
        String idQuery = "eq." + targetUserId;
        apiService.getPerfilByUserId(idQuery).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentPerfil = response.body().get(0);
                    etNombre.setText(currentPerfil.nombreDeUsuario);
                    tvEmail.setText(currentPerfil.correo);
                    etBio.setText(currentPerfil.biografia);
                } else {
                    Toast.makeText(PerfilActivity.this, "Error: No se encontró el perfil.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Perfil>> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Error de red al cargar el perfil: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Configura los listeners para los botones de actualizar y eliminar cuenta
    private void setupClickListeners() {
        if (!isMyProfile) {
            return;
        }

        btnActualizar.setOnClickListener(v -> {
            if (currentPerfil == null || currentPerfil.idPerfil == null) {
                Toast.makeText(PerfilActivity.this, "No se puede actualizar, el perfil no se ha cargado.", Toast.LENGTH_SHORT).show();
                return;
            }

            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevaBio = etBio.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombre.setError("El nombre no puede estar vacío");
                return;
            }

            currentPerfil.nombreDeUsuario = nuevoNombre;
            currentPerfil.nombre = nuevoNombre;
            currentPerfil.biografia = nuevaBio;

            String idQuery = "eq." + currentPerfil.idPerfil;

            apiService.updatePerfil(idQuery, currentPerfil).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PerfilActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = "Código: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                errorMsg += " - " + response.errorBody().string();
                            }
                        } catch (IOException e) { /* ignorar */ }
                        Log.e("UpdatePerfil", "Error al actualizar: " + errorMsg);
                        Toast.makeText(PerfilActivity.this, "Error al actualizar el perfil", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("UpdatePerfil", "Fallo de red", t);
                    Toast.makeText(PerfilActivity.this, "Fallo de red al intentar actualizar", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnEliminar.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    // Muestra un diálogo de confirmación antes de eliminar la cuenta permanentemente
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción es irreversible y borrará todas tus recetas, comentarios y favoritos.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Llama directamente a borrar el usuario. La DB se encarga del resto
                    deleteUserAccount();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Llama a la API para eliminar la cuenta del usuario actual
    private void deleteUserAccount() {
        String idQuery = "eq." + loggedInUserId;
        apiService.deleteUsuario(idQuery).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Si el usuario se borra, la DB borra el resto en cascada
                    logoutAndGoToLogin();
                } else {
                    // Si esto falla ahora, es un error del servidor o de red
                    handleDeleteError(response, "cuenta");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                handleConnectionError(t, "cuenta");
            }
        });
    }

    // Cierra la sesión del usuario y navega a la pantalla de Login
    private void logoutAndGoToLogin() {
        SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("userId").apply();

        Toast.makeText(this, "Cuenta eliminada exitosamente", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Maneja los errores de respuesta de la API durante la eliminación
    private void handleDeleteError(Response<Void> response, String entity) {
        String errorMsg = "Código: " + response.code();
        try {
            if (response.errorBody() != null) {
                errorMsg += " - " + response.errorBody().string();
            }
        } catch (IOException e) { /* ignorar */ }
        Log.e("DeleteAccount", "Error al eliminar " + entity + ": " + errorMsg);
        Toast.makeText(this, "Error al eliminar la " + entity + ". Revisa el Logcat para más detalles.", Toast.LENGTH_LONG).show();
    }

    // Maneja los errores de conexión de red durante la eliminación
    private void handleConnectionError(Throwable t, String entity) {
        Log.e("DeleteAccount", "Fallo de red al eliminar la " + entity, t);
        Toast.makeText(this, "Fallo de red al intentar eliminar la " + entity, Toast.LENGTH_SHORT).show();
    }
}
