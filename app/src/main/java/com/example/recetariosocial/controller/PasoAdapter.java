package com.example.recetariosocial.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetariosocial.R;
import com.example.recetariosocial.model.Paso;

import java.util.ArrayList;
import java.util.List;

public class PasoAdapter extends RecyclerView.Adapter<PasoAdapter.ViewHolder> {

    private List<Paso> pasos = new ArrayList<>();
    private OnDeleteListener onDeleteListener;
    private final boolean showDeleteButton;

    // Interfaz para notificar cuando un ítem es eliminado
    public interface OnDeleteListener {
        void onDelete(int position);
    }

    // Registra un callback para ser invocado cuando un ítem es eliminado
    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    // Constructor que muestra el botón de eliminar (para crear/editar recetas)
    public PasoAdapter() {
        this.showDeleteButton = true;
    }

    // Constructor que configura la visibilidad del botón de eliminar (para detalles)
    public PasoAdapter(boolean showDeleteButton) {
        this.showDeleteButton = showDeleteButton;
    }

    // Establece o actualiza la lista completa de pasos
    public void setPasos(List<Paso> pasos) {
        this.pasos = pasos;
        notifyDataSetChanged();
    }

    // Añade un nuevo paso a la lista
    public void addPaso(Paso paso) {
        pasos.add(paso);
        notifyItemInserted(pasos.size() - 1);
    }

    // Elimina un paso de la lista en una posición específica
    public void removePaso(int position) {
        if (position >= 0 && position < pasos.size()) {
            pasos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, pasos.size());
        }
    }

    // Devuelve la lista actual de pasos
    public List<Paso> getPasos() {
        return pasos;
    }

    // Crea y devuelve un nuevo ViewHolder inflando el layout para cada item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paso_add, parent, false);
        return new ViewHolder(view);
    }

    // Vincula los datos de un paso a las vistas de un ViewHolder específico
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paso paso = pasos.get(position);
        holder.tvPasoNumero.setText((position + 1) + ".");
        holder.tvPasoDescripcion.setText(paso.descripcion);

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

    // Devuelve el número total de pasos en la lista
    @Override
    public int getItemCount() {
        return pasos.size();
    }

    // Clase interna que representa cada item visual del RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPasoNumero, tvPasoDescripcion;
        ImageButton btnEliminar;

        // Constructor del ViewHolder: inicializa las vistas del item
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPasoNumero = itemView.findViewById(R.id.tvNumeroPaso);
            tvPasoDescripcion = itemView.findViewById(R.id.tvDescripcionPaso);
            btnEliminar = itemView.findViewById(R.id.btnEliminarPaso);
        }
    }
}
