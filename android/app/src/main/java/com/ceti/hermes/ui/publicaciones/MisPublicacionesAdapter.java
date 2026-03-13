package com.ceti.hermes.ui.publicaciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ceti.hermes.R;
import com.ceti.hermes.data.models.Publicacion;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MisPublicacionesAdapter extends RecyclerView.Adapter<MisPublicacionesAdapter.ViewHolder> {

    private List<Publicacion> publicaciones = new ArrayList<>();
    private OnPublicacionActionListener listener;
    private String baseUrl;

    public interface OnPublicacionActionListener {
        void onEditarClick(Publicacion publicacion);
        void onEliminarClick(Publicacion publicacion);
        void onPublicacionClick(Publicacion publicacion);
    }

    public MisPublicacionesAdapter(String baseUrl, OnPublicacionActionListener listener) {
        this.baseUrl = baseUrl;
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

        // Cargar primera foto
        if (publicacion.getFotos() != null && !publicacion.getFotos().isEmpty()) {
            String fotoUrl = baseUrl + "/uploads/book-pictures/" + publicacion.getFotos().get(0);
            Glide.with(holder.imgLibro.getContext())
                    .load(fotoUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.imgLibro);
        } else {
            holder.imgLibro.setImageResource(R.mipmap.ic_launcher);
        }

        // Listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPublicacionClick(publicacion);
            }
        });

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarClick(publicacion);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(publicacion);
            }
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLibro;
        TextView tvTitulo, tvPrecio, tvEstado;
        MaterialButton btnEditar, btnEliminar;

        ViewHolder(View itemView) {
            super(itemView);
            imgLibro = itemView.findViewById(R.id.imgLibro);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}