package com.example.recetariosocial.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Ingrediente;

import java.util.ArrayList;
import java.util.List;

public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    private List<Ingrediente> ingredientes = new ArrayList<>();
    private OnDeleteListener onDeleteListener;
    private final boolean showDeleteButton;

    //  Interfaz para notificar cuando un ítem es eliminado
    public interface OnDeleteListener {
        void onDelete(int position);
    }

    //Registra un callback para ser invocado cuando un ítem es eliminado @param listener El callback que se ejecutará
    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    // Constructor por defecto, muestra el botón de eliminar (usado en la creación de recetas)
    public IngredienteAdapter() {
        this.showDeleteButton = true;
    }

    //  Constructor que permite configurar la visibilidad del botón de eliminar @param showDeleteButton True para mostrar el botón, false para ocultarlo (usado en detalles)
    public IngredienteAdapter(boolean showDeleteButton) {
        this.showDeleteButton = showDeleteButton;
    }

    //Establece o actualiza la lista completa de ingredientes y notifica al adaptador @param ingredientes La nueva lista de ingredientes
    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
        notifyDataSetChanged();
    }

    //  Añade un nuevo ingrediente a la lista y notifica al adaptador @param ingrediente El ingrediente a añadir
    public void addIngrediente(Ingrediente ingrediente) {
        ingredientes.add(ingrediente);
        notifyItemInserted(ingredientes.size() - 1);
    }

    //  Elimina un ingrediente de la lista en una posición específica @param position La posición del ingrediente a eliminar
    public void removeIngrediente(int position) {
        if (position >= 0 && position < ingredientes.size()) {
            ingredientes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, ingredientes.size());
        }
    }

    //  Devuelve la lista actual de ingredientes @return La lista de ingredientes
    public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    //  Crea y devuelve un nuevo ViewHolder inflando el layout para cada item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingrediente_add, parent, false);
        return new ViewHolder(view);
    }

    //  Vincula los datos de un ingrediente a las vistas de un ViewHolder específico
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingrediente ingrediente = ingredientes.get(position);
        holder.tvNombre.setText(ingrediente.nombre);
        holder.tvCantidad.setText(ingrediente.cantidad);

        // Muestra u oculta el botón de eliminar según la configuración del constructor
        if (showDeleteButton) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> {
                if (onDeleteListener != null) {
                    onDeleteListener.onDelete(holder.getAdapterPosition());
                }
            });
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
        }
    }

    // Devuelve el número total de ingredientes en la lista
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    // Clase interna que representa cada item visual del RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCantidad;
        ImageButton btnEliminar;

        // Constructor del ViewHolder: inicializa las vistas del item
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreIngrediente);
            tvCantidad = itemView.findViewById(R.id.tvCantidadIngrediente);
            btnEliminar = itemView.findViewById(R.id.btnEliminarIngrediente);
        }
    }
}
