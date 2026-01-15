package com.example.recetariosocial.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Comentario;
import com.example.recetariosocial.model.Perfil;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private List<Comentario> comentarios = new ArrayList<>();
    private ApiService apiService;

    // Constructor: inicializa el servicio de la API
    public ComentarioAdapter() {
        apiService = SupabaseClient.getClient().create(ApiService.class);
    }

    // Establece o actualiza la lista completa de comentarios y notifica al adaptador
    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
        notifyDataSetChanged();
    }

    // Añade un nuevo comentario a la lista y notifica al adaptador
    public void addComentario(Comentario comentario) {
        this.comentarios.add(comentario);
        notifyItemInserted(comentarios.size() - 1);
    }

    // Crea y devuelve un nuevo ViewHolder inflando el layout para cada item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    // Vincula los datos de un comentario a las vistas de un ViewHolder específico
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);
        holder.tvTexto.setText(comentario.texto);

        // Carga el nombre de usuario de forma asíncrona usando el ID de usuario del comentario
        holder.tvUsuario.setText("Cargando...");
        apiService.getPerfilByUserId("eq." + comentario.idUsuario).enqueue(new Callback<List<Perfil>>() {
            @Override
            public void onResponse(Call<List<Perfil>> call, Response<List<Perfil>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    holder.tvUsuario.setText(response.body().get(0).nombreDeUsuario);
                } else {
                    holder.tvUsuario.setText("Usuario desconocido");
                }
            }

            @Override
            public void onFailure(Call<List<Perfil>> call, Throwable t) {
                holder.tvUsuario.setText("Error");
            }
        });
    }

    // Devuelve el número total de comentarios en la lista
    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    // Clase interna que representa cada item visual del RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsuario, tvTexto;

        // Constructor del ViewHolder: inicializa las vistas del item
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsuario = itemView.findViewById(R.id.tvUsuarioComentario);
            tvTexto = itemView.findViewById(R.id.tvTextoComentario);
        }
    }
}
