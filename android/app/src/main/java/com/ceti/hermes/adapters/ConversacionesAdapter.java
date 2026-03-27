package com.ceti.hermes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ceti.hermes.R;
import com.ceti.hermes.data.models.Conversacion;
import com.ceti.hermes.data.models.User;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.ui.chat.ConversacionActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversacionesAdapter extends RecyclerView.Adapter<ConversacionesAdapter.ConversacionViewHolder> {

    private List<Conversacion> conversaciones;
    private Context context;

    public ConversacionesAdapter(List<Conversacion> conversaciones, Context context) {
        this.conversaciones = conversaciones;
        this.context = context;
    }

    @NonNull
    @Override
    public ConversacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversacion, parent, false);
        return new ConversacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversacionViewHolder holder, int position) {
        Conversacion conversacion = conversaciones.get(position);

        // Determinar quién es el otro usuario (comprador o vendedor)
        User otroUsuario = null;
        int miUsuarioId = new com.ceti.hermes.utils.SessionManager(context).getUserId();

        if (conversacion.getCompradorId() == miUsuarioId) {
            otroUsuario = conversacion.getVendedor();
        } else {
            otroUsuario = conversacion.getComprador();
        }

        // Mostrar nombre del otro usuario
        if (otroUsuario != null) {
            holder.tvNombre.setText(otroUsuario.getNombre() + " " + otroUsuario.getApellido());

            // Cargar foto de perfil
            if (otroUsuario.getFotoPerfil() != null && !otroUsuario.getFotoPerfil().isEmpty()) {
                String fotoUrl = RetrofitClient.getProfilePicUrl(otroUsuario.getFotoPerfil());
                Glide.with(context)
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(holder.imgPerfil);
            } else {
                holder.imgPerfil.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        // Mostrar título del libro
        if (conversacion.getPublicacion() != null) {
            holder.tvLibro.setText(conversacion.getPublicacion().getTitulo());
        }

        // Mostrar último mensaje
        if (conversacion.getUltimoMensaje() != null && !conversacion.getUltimoMensaje().isEmpty()) {
            holder.tvUltimoMensaje.setText(conversacion.getUltimoMensaje());
        } else {
            holder.tvUltimoMensaje.setText("Sin mensajes");
        }

        // Mostrar fecha del último mensaje
        if (conversacion.getUltimoMensajeFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(conversacion.getUltimoMensajeFecha()));
        }

        // Click para abrir el chat
        User finalOtroUsuario = otroUsuario;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ConversacionActivity.class);
            intent.putExtra("conversacionId", conversacion.getId());
            if (finalOtroUsuario != null) {
                intent.putExtra("vendedorNombre", finalOtroUsuario.getNombre() + " " + finalOtroUsuario.getApellido());
                intent.putExtra("vendedorFoto", finalOtroUsuario.getFotoPerfil());
                intent.putExtra("otroUsuarioId", finalOtroUsuario.getId()); // ← AQUÍ antes del startActivity
            }
            if (conversacion.getPublicacion() != null) {
                intent.putExtra("tituloLibro", conversacion.getPublicacion().getTitulo());
            }
            context.startActivity(intent); // ← siempre al final
        });
    }

    @Override
    public int getItemCount() {
        return conversaciones.size();
    }

    public void actualizarConversaciones(List<Conversacion> nuevasConversaciones) {
        this.conversaciones = nuevasConversaciones;
        notifyDataSetChanged();
    }

    static class ConversacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPerfil;
        TextView tvNombre;
        TextView tvLibro;
        TextView tvUltimoMensaje;
        TextView tvFecha;

        public ConversacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPerfil = itemView.findViewById(R.id.imgPerfil);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvLibro = itemView.findViewById(R.id.tvLibro);
            tvUltimoMensaje = itemView.findViewById(R.id.tvUltimoMensaje);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}