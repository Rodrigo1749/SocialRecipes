package com.example.recetariosocial.network;

import com.example.recetariosocial.model.Comentario;
import com.example.recetariosocial.model.Estado;
import com.example.recetariosocial.model.Favorito;
import com.example.recetariosocial.model.Ingrediente;
import com.example.recetariosocial.model.Pais;
import com.example.recetariosocial.model.Paso;
import com.example.recetariosocial.model.Perfil;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // USUARIOS
    @GET("Usuarios")
    Call<List<Usuario>> login(@Query("correo") String email, @Query("contraseña") String password);

    @GET("Usuarios")
    Call<List<Usuario>> getUsuarioByEmail(@Query("correo") String emailQuery);

    @POST("Usuarios")
    Call<Void> registerUsuario(@Body Usuario usuario);

    @DELETE("Usuarios")
    Call<Void> deleteUsuario(@Query("idUsuario") String userIdQuery);

    //  PERFILES
    @GET("Perfiles")
    Call<List<Perfil>> getPerfilByUserId(@Query("idUsuario") String userId);

    @POST("Perfiles")
    Call<Void> createPerfil(@Body Perfil perfil);

    @PATCH("Perfiles")
    Call<Void> updatePerfil(@Query("idPerfil") String perfilIdQuery, @Body Perfil perfil);

    @DELETE("Perfiles")
    Call<Void> deletePerfil(@Query("idPerfil") String perfilIdQuery);

    //  RECETAS
    @GET("Recetas")
    Call<List<Receta>> getRecetas();

    @GET("Recetas")
    Call<List<Receta>> getRecetaById(@Query("IdReceta") String recetaId);

    @POST("Recetas")
    Call<List<Receta>> createReceta(@Body Receta receta);

    @PATCH("Recetas")
    Call<Void> updateReceta(@Query("IdReceta") String recetaIdQuery, @Body Receta receta);

    @DELETE("Recetas")
    Call<Void> deleteReceta(@Query("IdReceta") String recetaIdQuery);

    @GET("Recetas")
    Call<List<Receta>> searchRecetas(@Query("Titulo") String titleQuery);

    // Obtener múltiples recetas por sus IDs
    @GET("Recetas")
    Call<List<Receta>> getRecetasByIds(@Query("IdReceta") String recetaIds);

    // UBICACIONES
    @GET("Estados")
    Call<List<Estado>> getEstadosByPais(@Query("idPais") String paisId);

    @GET("Estados")
    Call<List<Estado>> getEstadoById(@Query("id_estado") String estadoId);

    @GET("Paises")
    Call<List<Pais>> getPaisById(@Query("id_pais") String paisId);

    //  RECETA DETALLES
    @GET("Ingredientes")
    Call<List<Ingrediente>> getIngredientesByReceta(@Query("idReceta") String recetaId);

    @POST("Ingredientes")
    Call<Void> createIngrediente(@Body Ingrediente ingrediente);

    @DELETE("Ingredientes")
    Call<Void> deleteIngredientesByReceta(@Query("idReceta") String recetaId);

    @GET("Pasos")
    Call<List<Paso>> getPasosByReceta(@Query("idReceta") String recetaId);

    @POST("Pasos")
    Call<Void> createPaso(@Body Paso paso);

    @DELETE("Pasos")
    Call<Void> deletePasosByReceta(@Query("idReceta") String recetaId);

    @GET("Comentarios")
    Call<List<Comentario>> getComentariosByReceta(@Query("idReceta") String recetaId);

    @POST("Comentarios")
    Call<Void> createComentario(@Body Comentario comentario);

    //  FAVORITOS
    @POST("Favoritos")
    Call<Void> addFavorito(@Body Favorito favorito);

    @DELETE("Favoritos")
    Call<Void> removeFavorito(@Query("idUsuario") String idUsuario, @Query("idReceta") String idReceta);

    @GET("Favoritos")
    Call<List<Favorito>> getFavoritosByUser(@Query("idUsuario") String idUsuario);
}