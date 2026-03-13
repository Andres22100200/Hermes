package com.ceti.hermes.ui.publicaciones;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.data.models.User;
import com.ceti.hermes.databinding.ActivityDetallePublicacionBinding;
import com.google.gson.Gson;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePublicacionActivity extends AppCompatActivity {

    private ActivityDetallePublicacionBinding binding;
    private FotosAdapter fotosAdapter;
    private Publicacion publicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetallePublicacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener ID de la publicación
        int publicacionId = getIntent().getIntExtra("publicacion_id", -1);

        if (publicacionId == -1) {
            Toast.makeText(this, "Error: ID de publicación inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViewPager();
        cargarPublicacion(publicacionId);
    }

    private void setupViewPager() {
        fotosAdapter = new FotosAdapter("http://192.168.100.5:3000");
        binding.viewPagerFotos.setAdapter(fotosAdapter);

        // Listener para actualizar el indicador de página
        binding.viewPagerFotos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                actualizarIndicadorFotos(position);
            }
        });
    }

    private void cargarPublicacion(int id) {
        mostrarLoading(true);

        Call<Map<String, Object>> call = RetrofitClient.getApiService().getPublicacion(id);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    Map<String, Object> publicacionMap = (Map<String, Object>) data.get("publicacion");

                    if (publicacionMap != null) {
                        // Convertir a objeto Publicacion
                        Gson gson = new Gson();
                        String json = gson.toJson(publicacionMap);
                        publicacion = gson.fromJson(json, Publicacion.class);

                        mostrarDatos();
                    }
                } else {
                    Toast.makeText(DetallePublicacionActivity.this,
                            "Error al cargar publicación",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(DetallePublicacionActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarDatos() {
        // Fotos
        if (publicacion.getFotos() != null && !publicacion.getFotos().isEmpty()) {
            fotosAdapter.setFotos(publicacion.getFotos());
            actualizarIndicadorFotos(0);
        }

        // Precio
        binding.tvPrecio.setText("$" + publicacion.getPrecio());

        // Título y autor
        binding.tvTitulo.setText(publicacion.getTitulo());
        binding.tvAutor.setText("por " + publicacion.getAutor());

        // Detalles
        binding.tvEstado.setText("Estado: " + publicacion.getEstadoLibro());

        if (!TextUtils.isEmpty(publicacion.getEditorial())) {
            binding.tvEditorial.setText("Editorial: " + publicacion.getEditorial());
            binding.tvEditorial.setVisibility(View.VISIBLE);
        } else {
            binding.tvEditorial.setVisibility(View.GONE);
        }

        if (publicacion.getYearPublicacion() != null) {
            binding.tvYear.setText("Año: " + publicacion.getYearPublicacion());
            binding.tvYear.setVisibility(View.VISIBLE);
        } else {
            binding.tvYear.setVisibility(View.GONE);
        }

        if (publicacion.getGeneros() != null && !publicacion.getGeneros().isEmpty()) {
            binding.tvGeneros.setText("Géneros: " + String.join(", ", publicacion.getGeneros()));
        }

        // Descripción
        if (!TextUtils.isEmpty(publicacion.getDescripcion())) {
            binding.tvDescripcion.setText(publicacion.getDescripcion());
        } else {
            binding.tvDescripcion.setText("Sin descripción");
        }

        // Punto de encuentro
        binding.tvPuntoEncuentro.setText("📍 " + publicacion.getPuntoEncuentro());

        // Vendedor
        if (publicacion.getVendedor() != null) {
            User vendedor = publicacion.getVendedor();

            binding.tvNombreVendedor.setText(vendedor.getNombre() + " " + vendedor.getApellido());

            String reputacion = vendedor.getPromedioEstrellas_vendedor() != null
                    ? vendedor.getPromedioEstrellas_vendedor() : "0.0";
            binding.tvReputacionVendedor.setText("⭐ " + reputacion + " valoraciones");

            // Foto del vendedor
            if (!TextUtils.isEmpty(vendedor.getFotoPerfil())) {
                String fotoUrl = "http://192.168.100.5:3000/uploads/profile-pictures/" + vendedor.getFotoPerfil();
                Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .error(android.R.drawable.ic_menu_myplaces)
                        .into(binding.imgVendedor);
            }
        }
    }

    private void actualizarIndicadorFotos(int position) {
        if (publicacion != null && publicacion.getFotos() != null) {
            int total = publicacion.getFotos().size();
            binding.tvIndicadorFotos.setText((position + 1) + " / " + total);
        }
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }
}