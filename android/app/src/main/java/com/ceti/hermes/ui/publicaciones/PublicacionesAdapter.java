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

import java.util.ArrayList;
import java.util.List;

public class PublicacionesAdapter extends RecyclerView.Adapter<PublicacionesAdapter.ViewHolder> {

    private List<Publicacion> publicaciones = new ArrayList<>();
    private OnPublicacionClickListener listener;
    private String baseUrl;

    public interface OnPublicacionClickListener {
        void onPublicacionClick(Publicacion publicacion);
    }

    public PublicacionesAdapter(String baseUrl, OnPublicacionClickListener listener) {
        this.baseUrl = baseUrl;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_publicacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publicacion publicacion = publicaciones.get(position);

        holder.tvTitulo.setText(publicacion.getTitulo());
        holder.tvAutor.setText(publicacion.getAutor());
        holder.tvEstado.setText(publicacion.getEstadoLibro());
        holder.tvPrecio.setText("$" + publicacion.getPrecio());
        holder.tvPuntoEncuentro.setText("📍 " + publicacion.getPuntoEncuentro());

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPublicacionClick(publicacion);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLibro;
        TextView tvTitulo, tvAutor, tvEstado, tvPrecio, tvPuntoEncuentro;

        ViewHolder(View itemView) {
            super(itemView);
            imgLibro = itemView.findViewById(R.id.imgLibro);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvPuntoEncuentro = itemView.findViewById(R.id.tvPuntoEncuentro);
        }
    }
}