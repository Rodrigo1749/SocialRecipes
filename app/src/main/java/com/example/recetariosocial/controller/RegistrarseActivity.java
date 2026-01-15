package com.example.recetariosocial.controller;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Perfil;
import com.example.recetariosocial.model.Usuario;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarseActivity extends AppCompatActivity {

    private EditText etNombreDeUsuario, etEmail,  etPassword;
    private Button btnLogin;
    private ApiService apiService;


    // Método principal que se llama al crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        // Inicializar cliente Retrofit
        apiService = SupabaseClient.getClient().create(ApiService.class);

        initViews();
        setupClickListener();
    }

    // Inicializa las vistas (componentes de la UI) del layout
    private void initViews(){
        etNombreDeUsuario = findViewById(R.id.etNombreDeUsuario);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
    }

    // Configura el listener para el evento de clic en el botón de registro
    private void setupClickListener(){
        btnLogin.setOnClickListener(view -> {
            String nombreUsuario = etNombreDeUsuario.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(validateForm( nombreUsuario,email,password)){
                registrarUsuario(nombreUsuario, email, password);
            }
        });
    }

    // Prepara el objeto Usuario y comienza el flujo de creación en la base de datos
    private void registrarUsuario(String nombre, String email, String password) {
        // Crear el objeto Usuario para el nuevo registro
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.correo = email;
        nuevoUsuario.contraseña = password;
        nuevoUsuario.nombre = nombre;

        // Llamar a la creación del usuario
        crearUsuarioEnSupabase(nuevoUsuario, nombre);
    }
    
    // Llama a la API para crear un nuevo registro de Usuario
    private void crearUsuarioEnSupabase(Usuario usuario, String nombrePerfil) {
        apiService.registerUsuario(usuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Si el usuario se crea con éxito, procedemos a buscarlo para crear su perfil
                    buscarUsuarioYCrearPerfil(usuario.correo, nombrePerfil);
                } else if (response.code() == 409) {
                    // Si el usuario ya existe, intentamos el flujo de recuperación de cuenta
                    Toast.makeText(RegistrarseActivity.this, "El correo ya está registrado. Verificando...", Toast.LENGTH_SHORT).show();
                    intentarRecuperarUsuario(usuario, nombrePerfil);
                } else {
                    // Otros errores de red o servidor
                    Log.e("Registro", "Error al crear usuario. Código: " + response.code());
                    Toast.makeText(RegistrarseActivity.this, "Error al crear usuario: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Errores de conexión
                Toast.makeText(RegistrarseActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Si el correo ya existe, intenta hacer login para verificar si la contraseña es correcta
    private void intentarRecuperarUsuario(Usuario usuario, String nombrePerfil) {

        String emailQuery = "eq." + usuario.correo;
        String passwordQuery = "eq." + usuario.contraseña;
        
        // Llamar al método login para verificar las credenciales
        apiService.login(emailQuery, passwordQuery).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Si las credenciales son correctas, obtenemos el usuario y creamos el perfil si no existe
                    Usuario usuarioRecuperado = response.body().get(0);
                    verificarYCrearPerfil(usuarioRecuperado, nombrePerfil);
                } else {
                    // Si las credenciales no coinciden, informamos al usuario
                    Toast.makeText(RegistrarseActivity.this, "El correo ya existe, pero la contraseña es incorrecta.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(RegistrarseActivity.this, "Error al verificar el usuario existente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Comprueba si un usuario recuperado ya tiene un perfil antes de crear uno nuevo
    private void verificarYCrearPerfil(Usuario usuario, String nombrePerfil) {
        // Verificar si el perfil ya existe
        apiService.getPerfilByUserId("eq." + usuario.idUsuario).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // El perfil ya existe, el registro está completo
                    Toast.makeText(RegistrarseActivity.this, "Tu cuenta ya existía. Registro completado.", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad de registro
                } else {
                    // El perfil no existe, procedemos a crearlo
                    crearPerfilParaUsuario(usuario, nombrePerfil);
                }
            }

            @Override
            public void onFailure(Call<List<Perfil>> call, Throwable t) {
                crearPerfilParaUsuario(usuario, nombrePerfil);
            }
        });
    }

    // Busca un usuario por su email para obtener el ID y poder crear su perfil asociado
    private void buscarUsuarioYCrearPerfil(String email, String nombrePerfil) {
        // Buscar al usuario recién creado por su correo para obtener su ID
         apiService.getUsuarioByEmail("eq." + email).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Usuario usuarioCreado = response.body().get(0);
                    crearPerfilParaUsuario(usuarioCreado, nombrePerfil);
                } else {
                    Toast.makeText(RegistrarseActivity.this, "Usuario creado, pero no se pudo encontrar para crear el perfil.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Toast.makeText(RegistrarseActivity.this, "Error al recuperar el usuario recién creado.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Prepara el objeto Perfil con los datos del usuario para su creación
    private void crearPerfilParaUsuario(Usuario usuario, String nombrePerfil) {
        // Crear el objeto Perfil
        Perfil nuevoPerfil = new Perfil();
        nuevoPerfil.idUsuario = usuario.idUsuario;
        nuevoPerfil.nombreDeUsuario = nombrePerfil;
        nuevoPerfil.nombre = nombrePerfil;
        nuevoPerfil.correo = usuario.correo;
        nuevoPerfil.contraseña = usuario.contraseña;
        nuevoPerfil.biografia = ""; // Biografía inicial vacía
        
        // Llamar a la creación del perfil en Supabase
        crearPerfilEnSupabase(nuevoPerfil);
    }
    
    // Llama a la API para crear un nuevo registro de Perfil en la base de datos
    private void crearPerfilEnSupabase(Perfil perfil) {
        apiService.createPerfil(perfil).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 409) { // 201 Created o 409 Conflict (si ya existe)
                    Toast.makeText(RegistrarseActivity.this, "¡Registrado exitosamente!", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad y vuelve al login
                } else {
                    String errorMsg = "Error desconocido";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("Perfil", "Error al crear perfil. Código: " + response.code() + " Body: " + errorMsg);
                    Toast.makeText(RegistrarseActivity.this, "Error al crear el perfil: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                 Toast.makeText(RegistrarseActivity.this, "Error de red al crear el perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Valida que los campos del formulario de registro no estén vacíos y tengan el formato correcto
    private boolean validateForm( String nombreDeUsuario, String email, String password){
        boolean isValid = true;

        if (nombreDeUsuario == null || nombreDeUsuario.isEmpty()){
            etNombreDeUsuario.setError(getString(R.string.activity_main_usuario_validate));
            isValid = false;
        }else{
           etNombreDeUsuario.setError(null);
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError(getString(R.string.activity_main_email_validate));
            isValid = false;
        }else{
            etEmail.setError(null);
        }

        if(password.length() < 8){
            etPassword.setError(getString(R.string.activity_main_password_validate));
            isValid = false;
        }else{
            etPassword.setError(null);
        }

        return isValid;
    }
}
