package com.example.recetariosocial.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Estado;
import com.example.recetariosocial.model.Ingrediente;
import com.example.recetariosocial.model.Paso;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistraReceta extends AppCompatActivity {

    private EditText etTituloReceta, etDescripcion;
    private TextView tvTextoImagen;
    private ImageView ivReceta;
    private Spinner spEstados;
    private RecyclerView rvIngredientes, rvPasos;
    private Button btnAgregarIngrediente, btnAgregarPasos, btnCrearReceta;

    private ApiService apiService;
    private IngredienteAdapter ingredienteAdapter;
    private PasoAdapter pasoAdapter;
    private List<Estado> listaEstados = new ArrayList<>();
    private Uri selectedImageUri;

    private boolean isEditMode = false;
    private int editingRecipeId = -1;
    private Receta recetaActual;

    // Maneja el resultado de seleccionar una imagen de la galería
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivReceta);
                    tvTextoImagen.setText("Imagen seleccionada");
                }
            }
    );

    // Método principal que se llama al crear la actividad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_receta);

        apiService = SupabaseClient.getClient().create(ApiService.class);

        initViews();
        setupAdapters();
        loadEstados(); // Carga los estados primero
        setupClickListeners();

        // Comprueba si la actividad se inició en modo de edición
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EDIT_RECIPE_ID")) {
            editingRecipeId = intent.getIntExtra("EDIT_RECIPE_ID", -1);
            if (editingRecipeId != -1) {
                isEditMode = true;
                setupEditMode();
            }
        }
    }

    // Inicializa las vistas (componentes de la UI) del layout
    private void initViews() {
        etTituloReceta = findViewById(R.id.etTituloReceta);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvTextoImagen = findViewById(R.id.tvTextoImagen);
        ivReceta = findViewById(R.id.ivReceta);
        spEstados = findViewById(R.id.spEstados);
        rvIngredientes = findViewById(R.id.rwIngredientes);
        rvPasos = findViewById(R.id.rwPasos);
        btnAgregarIngrediente = findViewById(R.id.btnAgregarIngrediente);
        btnAgregarPasos = findViewById(R.id.btnAgregarPasos);
        btnCrearReceta = findViewById(R.id.btnCrearReceta);
    }

    // Configura los adaptadores para las listas de ingredientes y pasos
    private void setupAdapters() {
        ingredienteAdapter = new IngredienteAdapter(true);
        rvIngredientes.setLayoutManager(new LinearLayoutManager(this));
        rvIngredientes.setAdapter(ingredienteAdapter);
        ingredienteAdapter.setOnDeleteListener(position -> ingredienteAdapter.removeIngrediente(position));

        pasoAdapter = new PasoAdapter(true);
        rvPasos.setLayoutManager(new LinearLayoutManager(this));
        rvPasos.setAdapter(pasoAdapter);
        pasoAdapter.setOnDeleteListener(position -> pasoAdapter.removePaso(position));
    }

    // Carga la lista de estados desde la API para rellenar el Spinner
    private void loadEstados() {
        apiService.getEstadosByPais("eq.1").enqueue(new retrofit2.Callback<List<Estado>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Estado>> call, retrofit2.Response<List<Estado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaEstados = response.body();
                    ArrayAdapter<Estado> adapter = new ArrayAdapter<>(RegistraReceta.this, android.R.layout.simple_spinner_item, listaEstados);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spEstados.setAdapter(adapter);

                    if (isEditMode) {
                        loadRecipeForEditing();
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<List<Estado>> call, Throwable t) {
                Toast.makeText(RegistraReceta.this, "Error cargando estados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Asigna las acciones de clic a los botones e imágenes interactivas
    private void setupClickListeners() {
        ivReceta.setOnClickListener(v -> pickImage.launch("image/*"));
        tvTextoImagen.setOnClickListener(v -> pickImage.launch("image/*"));
        btnAgregarIngrediente.setOnClickListener(v -> showAddIngredienteDialog());
        btnAgregarPasos.setOnClickListener(v -> showAddPasoDialog());

        btnCrearReceta.setOnClickListener(v -> {
            if (isEditMode) {
                startUpdateProcess();
            } else {
                saveReceta(); // Lógica original de creación
            }
        });
    }

    // Configura la UI para el modo de edición (título y texto del botón)
    private void setupEditMode() {
        setTitle("Editar Receta");
        btnCrearReceta.setText("Actualizar Receta");
    }

    // Carga los datos de la receta a editar desde la API y rellena los campos
    private void loadRecipeForEditing() {
        apiService.getRecetaById("eq." + editingRecipeId).enqueue(new retrofit2.Callback<List<Receta>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Receta>> call, retrofit2.Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    recetaActual = response.body().get(0);

                    etTituloReceta.setText(recetaActual.Titulo);
                    etDescripcion.setText(recetaActual.Descripcion);

                    if (recetaActual.ImagenUrl != null && !recetaActual.ImagenUrl.isEmpty()) {
                        Glide.with(RegistraReceta.this).load(recetaActual.ImagenUrl).into(ivReceta);
                        tvTextoImagen.setText("Imagen cargada");
                    }

                    for (int i = 0; i < listaEstados.size(); i++) {
                        if (listaEstados.get(i).idEstado == recetaActual.IdEstado) {
                            spEstados.setSelection(i);
                            break;
                        }
                    }

                    apiService.getIngredientesByReceta("eq." + editingRecipeId).enqueue(new retrofit2.Callback<List<Ingrediente>>() {
                        @Override public void onResponse(retrofit2.Call<List<Ingrediente>> call, retrofit2.Response<List<Ingrediente>> response) {
                            if (response.isSuccessful() && response.body() != null) ingredienteAdapter.setIngredientes(response.body());
                        }
                        @Override public void onFailure(retrofit2.Call<List<Ingrediente>> call, Throwable t) {}
                    });

                    apiService.getPasosByReceta("eq." + editingRecipeId).enqueue(new retrofit2.Callback<List<Paso>>() {
                        @Override public void onResponse(retrofit2.Call<List<Paso>> call, retrofit2.Response<List<Paso>> response) {
                            if (response.isSuccessful() && response.body() != null) pasoAdapter.setPasos(response.body());
                        }
                        @Override public void onFailure(retrofit2.Call<List<Paso>> call, Throwable t) {}
                    });
                } else { finish(); }
            }
            @Override public void onFailure(retrofit2.Call<List<Receta>> call, Throwable t) { finish(); }
        });
    }

    // Inicia el proceso de actualización, validando los campos antes de continuar
    private void startUpdateProcess() {
        String titulo = etTituloReceta.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || spEstados.getSelectedItem() == null) {
            Toast.makeText(this, "Título, descripción y estado son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCrearReceta.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndUpdateReceta(titulo, descripcion);
        } else {
            updateReceta(titulo, descripcion, recetaActual.ImagenUrl);
        }
    }

    // Sube una nueva imagen (si la hay) y luego actualiza los datos de la receta
    private void uploadImageAndUpdateReceta(String titulo, String descripcion) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead; while ((bytesRead = inputStream.read(buffer)) != -1) { byteStream.write(buffer, 0, bytesRead); }
            byte[] fileBytes = byteStream.toByteArray();
            String fileName = "public/" + UUID.randomUUID().toString();

            Request request = new Request.Builder()
                    .url(SupabaseClient.PROJECT_URL + "/storage/v1/object/imagenes-recetas/" + fileName)
                    .post(RequestBody.create(fileBytes, MediaType.parse(getContentResolver().getType(selectedImageUri))))
                    .build();

            SupabaseClient.getHttpClient().newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { runOnUiThread(() -> btnCrearReceta.setEnabled(true)); }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String publicUrl = SupabaseClient.PROJECT_URL + "/storage/v1/object/public/imagenes-recetas/" + fileName;
                        runOnUiThread(() -> updateReceta(titulo, descripcion, publicUrl));
                    } else { runOnUiThread(() -> btnCrearReceta.setEnabled(true)); }
                }
            });
        } catch (IOException e) { btnCrearReceta.setEnabled(true); }
    }

    // Envía la petición a la API para actualizar los datos principales de la receta
    private void updateReceta(String titulo, String descripcion, String imageUrl) {
        recetaActual.Titulo = titulo;
        recetaActual.Descripcion = descripcion;
        recetaActual.IdEstado = ((Estado) spEstados.getSelectedItem()).idEstado;
        recetaActual.ImagenUrl = imageUrl;

        String idQuery = "eq." + recetaActual.IdReceta;

        apiService.updateReceta(idQuery, recetaActual).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    cleanAndSaveIngredientsAndSteps(recetaActual.IdReceta);
                } else {
                    btnCrearReceta.setEnabled(true);
                    Toast.makeText(RegistraReceta.this, "Error al actualizar la receta", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) { btnCrearReceta.setEnabled(true); }
        });
    }

    // Borra los ingredientes y pasos antiguos antes de guardar los nuevos
    private void cleanAndSaveIngredientsAndSteps(Integer recetaId) {
        String idQuery = "eq." + recetaId;
        apiService.deleteIngredientesByReceta(idQuery).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {}
            @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
        });
        apiService.deletePasosByReceta(idQuery).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {}
            @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
        });

        saveIngredientesYPasos(recetaId);
    }

    // Muestra un diálogo para que el usuario añada un nuevo ingrediente
    private void showAddIngredienteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Ingrediente");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_ingrediente, null);
        final EditText inputNombre = viewInflated.findViewById(R.id.input_nombre);
        final EditText inputCantidad = viewInflated.findViewById(R.id.input_cantidad);

        builder.setView(viewInflated);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString();
            String cantidad = inputCantidad.getText().toString();
            if (!nombre.isEmpty()) {
                ingredienteAdapter.addIngrediente(new Ingrediente(nombre, cantidad));
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Muestra un diálogo para que el usuario añada un nuevo paso
    private void showAddPasoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Paso");

        final EditText input = new EditText(this);
        input.setHint("Descripción del paso");
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String descripcion = input.getText().toString();
            if (!descripcion.isEmpty()) {
                pasoAdapter.addPaso(new Paso(descripcion));
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Inicia el proceso de creación de una receta, validando los campos
    private void saveReceta() {
        String titulo = etTituloReceta.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || spEstados.getSelectedItem() == null) {
            Toast.makeText(this, "Título, descripción y estado son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCrearReceta.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndCreateReceta(titulo, descripcion);
        } else {
            createReceta(titulo, descripcion, "");
        }
    }

    // Sube una imagen (si la hay) y luego crea la nueva receta
    private void uploadImageAndCreateReceta(String titulo, String descripcion) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }
            byte[] fileBytes = byteStream.toByteArray();
            String fileName = "public/" + UUID.randomUUID().toString();

            OkHttpClient client = SupabaseClient.getHttpClient();
            String url = SupabaseClient.PROJECT_URL + "/storage/v1/object/imagenes-recetas/" + fileName;

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(fileBytes, MediaType.parse(getContentResolver().getType(selectedImageUri))))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("UploadImage", "Fallo en la llamada a Supabase Storage", e);
                    runOnUiThread(() -> {
                        Toast.makeText(RegistraReceta.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        btnCrearReceta.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String publicUrl = SupabaseClient.PROJECT_URL + "/storage/v1/object/public/imagenes-recetas/" + fileName;
                        runOnUiThread(() -> createReceta(titulo, descripcion, publicUrl));
                    } else {
                        String errorBody = response.body().string();
                        Log.e("UploadImage", "Error al guardar imagen. Código: " + response.code() + " Body: " + errorBody);
                        runOnUiThread(() -> {
                            Toast.makeText(RegistraReceta.this, "Error al guardar la imagen en Supabase", Toast.LENGTH_SHORT).show();
                            btnCrearReceta.setEnabled(true);
                        });
                    }
                }
            });
        } catch (IOException e) {
            Log.e("UploadImage", "Error al leer el archivo de imagen local", e);
            Toast.makeText(this, "Error al leer el archivo de imagen", Toast.LENGTH_SHORT).show();
            btnCrearReceta.setEnabled(true);
        }
    }

    // Envía la petición a la API para crear la nueva receta en la base de datos
    private void createReceta(String titulo, String descripcion, String imageUrl) {
        SharedPreferences prefs = getSharedPreferences("RecetarioPrefs", Context.MODE_PRIVATE);
        Integer userId = prefs.getInt("userId", -1);

        Receta receta = new Receta();
        receta.Titulo = titulo;
        receta.Descripcion = descripcion;
        receta.IdUsuario = userId;
        receta.IdEstado = ((Estado) spEstados.getSelectedItem()).idEstado;
        receta.ImagenUrl = imageUrl;

        apiService.createReceta(receta).enqueue(new retrofit2.Callback<List<Receta>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Receta>> call, retrofit2.Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Receta recetaCreada = response.body().get(0);
                    saveIngredientesYPasos(recetaCreada.IdReceta);
                } else {
                     try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("CreateReceta", "Error al crear receta. Código: " + response.code() + " Body: " + errorBody);
                        Toast.makeText(RegistraReceta.this, "Error al crear la receta.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                         Log.e("CreateReceta", "Error leyendo el cuerpo del error", e);
                    }
                    btnCrearReceta.setEnabled(true);
                }
            }
            @Override
            public void onFailure(retrofit2.Call<List<Receta>> call, Throwable t) {
                Log.e("CreateReceta", "Fallo de red al crear receta", t);
                Toast.makeText(RegistraReceta.this, "Fallo de conexión al crear la receta.", Toast.LENGTH_SHORT).show();
                btnCrearReceta.setEnabled(true);
            }
        });
    }

    // Guarda los ingredientes y pasos asociados a una receta (para creación y actualización)
    private void saveIngredientesYPasos(Integer recetaId) {
        List<Ingrediente> listaIngredientes = ingredienteAdapter.getIngredientes();
        List<Paso> listaPasos = pasoAdapter.getPasos();

        for (Ingrediente ing : listaIngredientes) {
            ing.idReceta = recetaId;
            apiService.createIngrediente(ing).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {}
                @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
            });
        }

        for (int i = 0; i < listaPasos.size(); i++) {
            Paso paso = listaPasos.get(i);
            paso.idReceta = recetaId;
            paso.numeroPaso = i + 1;
            apiService.createPaso(paso).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {}
                @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
            });
        }

        Toast.makeText(this, isEditMode ? "Receta actualizada exitosamente" : "Receta publicada exitosamente", Toast.LENGTH_LONG).show();
        finish();
    }
}
