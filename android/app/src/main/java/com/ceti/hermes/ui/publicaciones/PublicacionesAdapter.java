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
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;

import java.util.ArrayList;
import java.util.List;

public class PublicacionesAdapter extends RecyclerView.Adapter<PublicacionesAdapter.ViewHolder> {

    private List<Publicacion> publicaciones = new ArrayList<>();
    private OnPublicacionClickListener listener;

    public interface OnPublicacionClickListener {
        void onPublicacionClick(Publicacion publicacion);
    }

    public PublicacionesAdapter(OnPublicacionClickListener listener) {
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

        // Opacidad y texto si está inactiva
        if ("Inactivo".equals(publicacion.getEstado())) {
            holder.itemView.setAlpha(0.5f);
            holder.tvTitulo.setText("Publicación desactivada");
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.tvTitulo.setText(publicacion.getTitulo());
        }

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