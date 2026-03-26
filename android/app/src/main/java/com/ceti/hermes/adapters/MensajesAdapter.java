package com.ceti.hermes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ceti.hermes.R;
import com.ceti.hermes.data.models.Mensaje;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensajesAdapter extends RecyclerView.Adapter<MensajesAdapter.MensajeViewHolder> {

    private List<Mensaje> mensajes = new ArrayList<>();
    private int miUsuarioId;

    public MensajesAdapter(int miUsuarioId) {
        this.miUsuarioId = miUsuarioId;
    }

    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
        notifyDataSetChanged();
    }

    public void agregarMensaje(Mensaje mensaje) {
        this.mensajes.add(mensaje);
        notifyItemInserted(mensajes.size() - 1);
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensaje, parent, false);
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);

        // Determinar tipo de mensaje
        if (mensaje.getTipo().equals("sistema")) {
            // Mensaje del sistema (centro)
            holder.layoutMensajePropio.setVisibility(View.GONE);
            holder.layoutMensajeOtro.setVisibility(View.GONE);
            holder.tvMensajeSistema.setVisibility(View.VISIBLE);
            holder.tvMensajeSistema.setText(mensaje.getContenido());

        } else if (mensaje.getRemitenteId() == miUsuarioId) {
            // Mensaje propio (derecha)
            holder.layoutMensajePropio.setVisibility(View.VISIBLE);
            holder.layoutMensajeOtro.setVisibility(View.GONE);
            holder.tvMensajeSistema.setVisibility(View.GONE);

            holder.tvMensajePropio.setText(mensaje.getContenido());
            holder.tvHoraPropio.setText(formatearHora(mensaje.getCreatedAt()));

        } else {
            // Mensaje del otro usuario (izquierda)
            holder.layoutMensajePropio.setVisibility(View.GONE);
            holder.layoutMensajeOtro.setVisibility(View.VISIBLE);
            holder.tvMensajeSistema.setVisibility(View.GONE);

            holder.tvMensajeOtro.setText(mensaje.getContenido());
            holder.tvHoraOtro.setText(formatearHora(mensaje.getCreatedAt()));
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    private String formatearHora(Date fecha) {
        if (fecha == null) return "";

        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(fecha);
        } catch (Exception e) {
            return "";
        }
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMensajePropio, layoutMensajeOtro;
        TextView tvMensajePropio, tvHoraPropio;
        TextView tvMensajeOtro, tvHoraOtro;
        TextView tvMensajeSistema;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutMensajePropio = itemView.findViewById(R.id.layoutMensajePropio);
            layoutMensajeOtro = itemView.findViewById(R.id.layoutMensajeOtro);

            tvMensajePropio = itemView.findViewById(R.id.tvMensajePropio);
            tvHoraPropio = itemView.findViewById(R.id.tvHoraPropio);

            tvMensajeOtro = itemView.findViewById(R.id.tvMensajeOtro);
            tvHoraOtro = itemView.findViewById(R.id.tvHoraOtro);

            tvMensajeSistema = itemView.findViewById(R.id.tvMensajeSistema);
        }
    }
}