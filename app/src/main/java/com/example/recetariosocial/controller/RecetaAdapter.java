package com.example.recetariosocial.controller;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Estado;
import com.example.recetariosocial.model.Pais;
import com.example.recetariosocial.model.Receta;
import com.example.recetariosocial.network.ApiService;
import com.example.recetariosocial.network.SupabaseClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Adaptador para mostrar una lista de objetos Receta en un RecyclerView
public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder> {

    private List<Receta> recetaList = new ArrayList<>();
    private OnItemClickListener listener;
    private Context context;
    private ApiService apiService;

    // Interfaz para manejar los clics en los items de la lista
    public interface OnItemClickListener {
        void onItemClick(Receta receta);
    }

    // Registra un listener para ser invocado cuando se hace clic en un ítem
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Establece o actualiza la lista de recetas y notifica al adaptador
    public void setRecetas(List<Receta> recetas) {
        this.recetaList = recetas;
        notifyDataSetChanged();
    }

    // Crea un nuevo ViewHolder inflando el layout para cada ítem de la receta
    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        apiService = SupabaseClient.getClient().create(ApiService.class);
        View view = LayoutInflater.from(context).inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(view);
    }

    // Vincula los datos de una receta específica a un ViewHolder
    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = recetaList.get(position);
        holder.bind(receta);
    }

    // Devuelve el número total de recetas en la lista
    @Override
    public int getItemCount() {
        return recetaList.size();
    }

    // Clase interna que representa la vista de cada ítem en el RecyclerView
    class RecetaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitulo;
        private TextView tvDescripcion;
        private TextView tvUbicacion;
        private ImageView ivImagen;

        // Constructor que inicializa las vistas del ítem y configura el listener de clic
        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvItemDescripcion);
            tvUbicacion = itemView.findViewById(R.id.tvItemUbicacion);
            ivImagen = itemView.findViewById(R.id.ivItemImagen);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(recetaList.get(position));
                }
            });
        }

        // Rellena las vistas del ítem con los datos de una receta específica
        public void bind(Receta receta) {
            tvTitulo.setText(receta.Titulo);
            tvDescripcion.setText(receta.Descripcion);

            // Carga la imagen de la receta usando Glide.
            if (receta.ImagenUrl != null && !receta.ImagenUrl.isEmpty()) {
                Glide.with(context)
                        .load(Uri.parse(receta.ImagenUrl))
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivImagen);
            } else {
                ivImagen.setImageResource(R.drawable.ic_launcher_background);
            }

            // Carga de forma asíncrona la ubicación (Estado y País) de la receta
            if (receta.IdEstado > 0) {
                tvUbicacion.setText("Cargando ubicación...");
                String estadoQuery = "eq." + receta.IdEstado;

                apiService.getEstadoById(estadoQuery).enqueue(new Callback<List<Estado>>() {
                    @Override
                    public void onResponse(Call<List<Estado>> call, Response<List<Estado>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Estado estado = response.body().get(0);
                            String paisQuery = "eq." + estado.idPais;

                            apiService.getPaisById(paisQuery).enqueue(new Callback<List<Pais>>() {
                                @Override
                                public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                                    String paisNombre = "";
                                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                        paisNombre = response.body().get(0).nombre;
                                    }
                                    tvUbicacion.setText(estado.nombre + (paisNombre.isEmpty() ? "" : ", " + paisNombre));
                                }

                                @Override
                                public void onFailure(Call<List<Pais>> call, Throwable t) {
                                    tvUbicacion.setText(estado.nombre); // Muestra al menos el estado si falla el país
                                }
                            });
                        } else {
                            tvUbicacion.setText("Ubicación desconocida");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Estado>> call, Throwable t) {
                        tvUbicacion.setText("Error de red");
                    }
                });
            } else {
                tvUbicacion.setText("Sin ubicación");
            }
        }
    }
}
