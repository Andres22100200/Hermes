package com.ceti.hermes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ceti.hermes.databinding.ActivityMainContainerBinding;
import com.ceti.hermes.ui.publicaciones.CrearPublicacionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainContainerBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Configurar Bottom Navigation
        BottomNavigationView bottomNav = binding.bottomNavigation;
        NavigationUI.setupWithNavController(bottomNav, navController);

// Manejar botones especiales (Vender y Perfil)
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_vender) {
                // Abrir CrearPublicacionActivity
                Intent intent = new Intent(MainActivity.this, CrearPublicacionActivity.class);
                startActivity(intent);
                return false; // No seleccionar este item

            } else if (itemId == R.id.perfilFragment) {
                // Abrir ProfileActivity
                Intent intent = new Intent(MainActivity.this, com.ceti.hermes.ui.main.ProfileActivity.class);
                startActivity(intent);
                return false; // No seleccionar este item

            } else {
                // Para los demás items (Inicio, Buscar, Favoritos), usar navegación normal
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }
}