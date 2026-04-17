package com.ceti.hermes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.databinding.ActivityMainContainerBinding;
import com.ceti.hermes.ui.publicaciones.CrearPublicacionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private ActivityMainContainerBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RetrofitClient.init(this);
        binding = ActivityMainContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Configurar Bottom Navigation
        BottomNavigationView bottomNav = binding.bottomNavigation;
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Configurar FAB para crear publicación
        FloatingActionButton fabCrearPublicacion = findViewById(R.id.fabCrearPublicacion);
        fabCrearPublicacion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearPublicacionActivity.class);
            startActivity(intent);
        });

// Manejar botones especiales (Vender y Perfil)
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.chatFragment) {
                // Chat usa navegación normal
                return NavigationUI.onNavDestinationSelected(item, navController);
            } else {
                // Todos los demás fragments incluyendo perfilFragment usan navegación normal
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }
}