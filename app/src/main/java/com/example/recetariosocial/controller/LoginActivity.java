package com.example.recetariosocial.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Usuario;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ApiService apiService;

    // Método principal que se llama al crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = SupabaseClient.getClient().create(ApiService.class);

        initViews();
        setupClickListener();
    }

    // Inicializa las vistas (componentes de la UI) del layout
    private void initViews(){
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
    }

    // Configura el listener para el evento de clic en el botón de login
    private void setupClickListener(){
        btnLogin.setOnClickListener(view -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(validateForm( email,password)){
                login(email, password);
            }

        });
    }

    // Realiza la llamada a la API para autenticar al usuario con su correo y contraseña
    private void login(String email, String password) {
        String emailQuery = "eq." + email;
        String passwordQuery = "eq." + password;

        Call<List<Usuario>> call = apiService.login(emailQuery, passwordQuery);
        
        call.enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario usuario = response.body().get(0);
                    
                    Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    
                    // Guardar sesión (ID del usuario)
                    SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
                    int userId = (usuario.idUsuario != null) ? usuario.idUsuario : 0;
                    prefs.edit().putInt("userId", userId).apply();
                    
                    // Ir a la pantalla principal de recetas
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Valida que el formato del correo y la longitud de la contraseña sean correctos
    private boolean validateForm( String email, String password){
        boolean isValid = true;


        //Validar correo electrónico
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError(getString(R.string.activity_main_email_validate));
            isValid = false;
        }else{
            etEmail.setError(null);
        }
        //Validar contraseña
        if(password.length() < 8){
            etPassword.setError(getString(R.string.activity_main_password_validate));
            isValid = false;
        }else{
            etPassword.setError(null);
        }

        return isValid;
    }
}
