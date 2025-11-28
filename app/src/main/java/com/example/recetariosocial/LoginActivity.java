package com.example.recetariosocial;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListener();
    }

    private void initViews(){
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
    }
    private void setupClickListener(){
        btnLogin.setOnClickListener(view -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(validateForm( email,password)){
                Toast.makeText(this, "Prueba exitosa", Toast.LENGTH_SHORT).show();
            }

        });
    }

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
