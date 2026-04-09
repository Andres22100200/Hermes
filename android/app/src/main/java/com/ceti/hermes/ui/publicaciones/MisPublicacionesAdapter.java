package com.ceti.hermes.ui.publicaciones;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ceti.hermes.R;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MisPublicacionesAdapter extends RecyclerView.Adapter<MisPublicacionesAdapter.ViewHolder> {

    private List<Publicacion> publicaciones = new ArrayList<>();
    private OnPublicacionActionListener listener;

    public interface OnPublicacionActionListener {
        void onEditarClick(Publicacion publicacion);
        void onEliminarClick(Publicacion publicacion);
        void onDesactivarClick(Publicacion publicacion);
        void onPublicacionClick(Publicacion publicacion);
    }

    public MisPublicacionesAdapter(OnPublicacionActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mi_publicacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publicacion publicacion = publicaciones.get(position);

        holder.tvTitulo.setText(publicacion.getTitulo());
        holder.tvPrecio.setText("$" + publicacion.getPrecio());
        holder.tvEstado.setText(publicacion.getEstado());

        // Color del estado
// Color del estado
        switch (publicacion.getEstado()) {
            case "Disponible":
                holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "Inactivo":
                holder.tvEstado.setTextColor(Color.GRAY);
                break;
            case "Reservado":
                holder.tvEstado.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "Vendido":
                holder.tvEstado.setTextColor(Color.parseColor("#2196F3"));
                break;
            case "Eliminado":
                holder.tvEstado.setTextColor(Color.parseColor("#F44336"));
                break;
            default:
                holder.tvEstado.setTextColor(Color.GRAY);
        }

// Opacidad según estado
        if ("Inactivo".equals(publicacion.getEstado())) {
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

// Botón desactivar
        if ("Disponible".equals(publicacion.getEstado())) {
            holder.btnDesactivar.setText("Desactivar");
        } else if ("Inactivo".equals(publicacion.getEstado())) {
            holder.btnDesactivar.setText("Activar");
        } else {
            holder.btnDesactivar.setText("Activar");
        }

        // Foto
        if (publicacion.getFotos() != null && !publicacion.getFotos().isEmpty()) {
            String fotoUrl = RetrofitClient.getBookPicUrl(publicacion.getFotos().get(0));
            Glide.with(holder.imgLibro.getContext())
                    .load(fotoUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.imgLibro);
        } else {
            holder.imgLibro.setImageResource(R.mipmap.ic_launcher);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPublicacionClick(publicacion);
        });

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(publicacion);
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminarClick(publicacion);
        });

        holder.btnDesactivar.setOnClickListener(v -> {
            if (listener != null) listener.onDesactivarClick(publicacion);
        });
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    public void setPublicaciones(List<Publicacion> publicaciones) {
        this.publicaciones = publicaciones;
        notifyDataSetChanged();
    }

    public void removePublicacion(int publicacionId) {
        for (int i = 0; i < publicaciones.size(); i++) {
            if (publicaciones.get(i).getId() == publicacionId) {
                publicaciones.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void actualizarEstado(int publicacionId, String nuevoEstado) {
        for (int i = 0; i < publicaciones.size(); i++) {
            if (publicaciones.get(i).getId() == publicacionId) {
                publicaciones.get(i).setEstado(nuevoEstado);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLibro;
        TextView tvTitulo, tvPrecio, tvEstado;
        MaterialButton btnEditar, btnEliminar, btnDesactivar;

        ViewHolder(View itemView) {
            super(itemView);
            imgLibro = itemView.findViewById(R.id.imgLibro);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnDesactivar = itemView.findViewById(R.id.btnDesactivar);
        }
    }
}