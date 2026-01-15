package com.example.recetariosocial.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {


    public static final String PROJECT_URL = "https://lgyaocvixgejqlukcyej.supabase.co";
    private static final String API_URL = PROJECT_URL + "/rest/v1/";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxneWFvY3ZpeGdlanFsdWtjeWVqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU5NDQyODksImV4cCI6MjA4MTUyMDI4OX0.3pFG7DMc8vafjQW5FBgpWSV3rUHbP8ayVjMQeSLOlHo";

    private static Retrofit retrofit = null;
    private static OkHttpClient httpClient = null; // Se convierte en un campo estático

    // Se extrae la construcción del cliente a un método para poder reutilizarlo
    private static void buildHttpClient() {
        if (httpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Cliente HTTP base con la autenticación de Supabase
            httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("apikey", SUPABASE_KEY)
                                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                                .build();
                        return chain.proceed(request);
                    })
                    .build();
        }
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            buildHttpClient(); // Llama al método para construir el cliente base

            // Se crea un cliente específico para Retrofit que añade los headers para JSON
            OkHttpClient retrofitClient = httpClient.newBuilder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Prefer", "return=representation")
                                .build();
                        return chain.proceed(request);
                    }).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(retrofitClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // NUEVO: Método para obtener el cliente OkHttp genérico para otras operaciones (como subir archivos)
    public static OkHttpClient getHttpClient() {
        buildHttpClient();
        return httpClient;
    }
}