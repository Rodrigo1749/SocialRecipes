package com.example.recetariosocial;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrarseActivity extends AppCompatActivity {

    private EditText etNombreDeUsuario, etEmail,  etPassword;
    private Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        initViews();
        setupClickListener();
    }

    private void initViews(){
        etNombreDeUsuario = findViewById(R.id.etNombreDeUsuario);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupClickListener(){
        btnLogin.setOnClickListener(view -> {
            String nombreUsuario = etNombreDeUsuario.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(validateForm( nombreUsuario,email,password)){
                Toast.makeText(this, "Prueba exitosa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm( String nombreDeUsuario, String email, String password){
        boolean isValid = true;

        //Validar nombre de usuario
        if (nombreDeUsuario == null){
            etNombreDeUsuario.setError(getString(R.string.activity_main_usuario_validate));
            isValid = false;
        }else{
           etNombreDeUsuario.setError(null);
        }
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
