package com.ceti.hermes.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ceti.hermes.ui.auth.login.LoginActivity;
import com.ceti.hermes.ui.main.ProfileActivity;
import com.ceti.hermes.utils.SessionManager;

public class PerfilFragment extends Fragment {

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());

        // Layout vertical
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setGravity(android.view.Gravity.CENTER);

        // Nombre del usuario
        TextView tvNombre = new TextView(getContext());
        tvNombre.setText("Hola, " + sessionManager.getUserName());
        tvNombre.setTextSize(24);
        tvNombre.setPadding(0, 0, 0, 32);
        layout.addView(tvNombre);

        // Botón ver perfil completo
        Button btnVerPerfil = new Button(getContext());
        btnVerPerfil.setText("Ver Perfil Completo");
        btnVerPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            startActivity(intent);
        });
        layout.addView(btnVerPerfil);

        // Botón cerrar sesión
        Button btnCerrarSesion = new Button(getContext());
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        layout.addView(btnCerrarSesion);

        return layout;
    }
}