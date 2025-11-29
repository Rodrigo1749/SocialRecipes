package com.example.recetariosocial;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistraReceta extends AppCompatActivity {

    private EditText etTituloReceta, etDescripcion, tvTextoImagen;
    private Button btnAgregarIngredientes;

    @Override
    protected void onCreate(Bundle savedIntanceState){
        super.onCreate(savedIntanceState);
        setContentView(R.layout.activity_crear_receta);

        initViews();
        setupClickListener();

    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        etTituloReceta = findViewById(R.id.etTituloReceta);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvTextoImagen = findViewById(R.id.tvTextoImagen);

        btnAgregarIngredientes = findViewById(R.id.btnAgregarIngrediente);

    }

    private void setupClickListener() {
        btnAgregarIngredientes.setOnClickListener(view ->{
            String Titulo = etTituloReceta.getText().toString().trim();
            String Descripcion = etDescripcion.getText().toString().trim();
            String ImagenUrl = tvTextoImagen.getText().toString().trim();

            if (validateForm(Titulo,Descripcion,ImagenUrl)){
                Toast.makeText(this,"Receta Registrada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm(String Titulo, String Descripcion, String ImagenUrl) {
        boolean isValid = true;

        //Validar Titulo de la receta
        if (Titulo == null){
            etTituloReceta.setError("El titulo de la receta no debe estar vacio");
            isValid = false;
        }else {
            etTituloReceta.setError(null);
        }

        //Validar la descripcion
        if (Descripcion == null){
            etDescripcion.setError("La descripcion no debe estar vacia");
            isValid = false;
        }else{
            etDescripcion.setError(null);
        }

        //Validar la imagen
        if (ImagenUrl == null){
            tvTextoImagen.setError("No se ha agregado ningun imagen");
            isValid = false;
        }else{
            tvTextoImagen.setError(null);
        }
        return isValid;
    }

}
