package com.ceti.hermes.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.MainActivity;
import com.ceti.hermes.databinding.ActivitySplashBinding;
import com.ceti.hermes.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private SessionManager sessionManager;

    // Tiempo de espera en el splash (2 segundos)
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar ViewBinding
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Esperar 2 segundos y luego verificar sesión
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            verificarSesion();
        }, SPLASH_DELAY);
    }

    /**
     * Verificar si hay sesión iniciada
     */
    private void verificarSesion() {
        if (sessionManager.isLoggedIn()) {
            // Usuario tiene sesión → Ir a MainActivity
            irAPantallaPrincipal();
        } else {
            // Usuario NO tiene sesión → Ir a Login
            irALogin();
        }
    }

    /**
     * Ir a la pantalla principal (MainActivity)
     */
    private void irAPantallaPrincipal() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerrar SplashActivity para que no vuelva atrás
    }

    /**
     * Ir a la pantalla de Login
     */
    private void irALogin() {
        // TODO: Crear LoginActivity en el siguiente paso
        // Por ahora vamos a MainActivity
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}