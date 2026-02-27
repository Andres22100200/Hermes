package com.ceti.hermes.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.databinding.ActivityHomeBinding;
import com.ceti.hermes.ui.auth.login.LoginActivity;
import com.ceti.hermes.utils.SessionManager;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar ViewBinding
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Verificar que haya sesión activa
        if (!sessionManager.isLoggedIn()) {
            irALogin();
            return;
        }

        // Mostrar información del usuario
        mostrarInfoUsuario();

        // Configurar listeners
        setupListeners();
    }

    private void mostrarInfoUsuario() {
        String nombre = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        binding.tvUserName.setText(nombre != null ? nombre : "Usuario");
        binding.tvUserEmail.setText(email != null ? email : "");
    }

    private void setupListeners() {
        // Botón de cerrar sesión
        binding.btnLogout.setOnClickListener(v -> {
            cerrarSesion();
        });

        // Botón de ver perfil
        binding.btnVerPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void cerrarSesion() {
        // Limpiar sesión
        sessionManager.logout();

        // Mostrar mensaje
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

        // Ir a Login
        irALogin();
    }

    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}