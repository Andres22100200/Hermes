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
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;

import java.util.Map;
import android.webkit.WebView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import com.ceti.hermes.ui.chat.ConversacionActivity;

public class DetallePublicacionActivity extends AppCompatActivity {

    private ActivityDetallePublicacionBinding binding;
    private FotosAdapter fotosAdapter;
    private Publicacion publicacion;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetallePublicacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int publicacionId = getIntent().getIntExtra("publicacion_id", -1);

        if (publicacionId == -1) {
            Toast.makeText(this, "Error: ID de publicación inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupViewPager();
        cargarPublicacion(publicacionId);
    }

    private void setupViewPager() {
        fotosAdapter = new FotosAdapter();
        binding.viewPagerFotos.setAdapter(fotosAdapter);

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
                        Gson gson = new Gson();
                        String json = gson.toJson(publicacionMap);
                        publicacion = gson.fromJson(json, Publicacion.class);
                        mostrarDatos();
                    }
                } else {
                    Toast.makeText(DetallePublicacionActivity.this,
                            "Error al cargar publicación", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(DetallePublicacionActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (!TextUtils.isEmpty(publicacion.getDescripcion())) {
            binding.tvDescripcion.setText(publicacion.getDescripcion());
        } else {
            binding.tvDescripcion.setText("Sin descripción");
        }

        binding.tvPuntoEncuentro.setText("📍 " + publicacion.getPuntoEncuentro());
// Cargar mapa estático

        if (publicacion.getCoordenadas() != null) {
            double lat = publicacion.getCoordenadas().getLat();
            double lng = publicacion.getCoordenadas().getLng();

            String html = "<!DOCTYPE html><html><head>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css'/>"
                    + "<style>body{margin:0;padding:0;} #map{width:100%;height:180px;}</style>"
                    + "</head><body>"
                    + "<div id='map'></div>"
                    + "<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>"
                    + "<script>"
                    + "var map = L.map('map', {zoomControl:false, dragging:false, scrollWheelZoom:false})"
                    + ".setView([" + lat + "," + lng + "], 16);"
                    + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);"
                    + "L.marker([" + lat + "," + lng + "]).addTo(map);"
                    + "</script>"
                    + "</body></html>";

            binding.imgMapa.getSettings().setJavaScriptEnabled(true);
            binding.imgMapa.getSettings().setDomStorageEnabled(true);
            binding.imgMapa.setVisibility(View.VISIBLE);
            binding.imgMapa.loadDataWithBaseURL("https://unpkg.com", html, "text/html", "UTF-8", null);
        }


        // Vendedor
        if (publicacion.getVendedor() != null) {
            User vendedor = publicacion.getVendedor();

            binding.tvNombreVendedor.setText(vendedor.getNombre() + " " + vendedor.getApellido());

            String reputacion = vendedor.getPromedioEstrellas_vendedor() != null
                    ? vendedor.getPromedioEstrellas_vendedor() : "0.0";
            binding.tvReputacionVendedor.setText("⭐ " + reputacion + " valoraciones");

            if (!TextUtils.isEmpty(vendedor.getFotoPerfil())) {
                String fotoUrl = RetrofitClient.getProfilePicUrl(vendedor.getFotoPerfil());
                Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .error(android.R.drawable.ic_menu_myplaces)
                        .into(binding.imgVendedor);
            }

            // Click en nombre o foto → abrir perfil del vendedor
            View.OnClickListener abrirPerfilVendedor = v -> {
                Intent intent = new Intent(DetallePublicacionActivity.this,
                        com.ceti.hermes.ui.usuario.PerfilUsuarioActivity.class);
                intent.putExtra("usuarioId", vendedor.getId());
                startActivity(intent);
            };

            binding.tvNombreVendedor.setOnClickListener(abrirPerfilVendedor);
            binding.imgVendedor.setOnClickListener(abrirPerfilVendedor);
        }

        configurarBotonContactar();
    }

    private void configurarBotonContactar() {
        SessionManager sessionManager = new SessionManager(this);
        int miUsuarioId = sessionManager.getUserId();

        int vendedorId = publicacion.getVendedor() != null ? publicacion.getVendedor().getId() : -999;
        android.util.Log.d("DEBUG_CHAT", "Mi ID: " + miUsuarioId + " | Vendedor ID: " + vendedorId);

        if (publicacion.getVendedor() != null &&
                publicacion.getVendedor().getId() == miUsuarioId) {
            binding.btnContactarVendedor.setVisibility(View.GONE);
            return;
        }

        binding.btnContactarVendedor.setVisibility(View.VISIBLE);
        binding.btnContactarVendedor.setOnClickListener(v -> iniciarConversacion());
    }

    private void iniciarConversacion() {
        binding.btnContactarVendedor.setEnabled(false);
        binding.btnContactarVendedor.setText("Iniciando chat...");

        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getBearerToken();

        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
        body.addProperty("publicacionId", publicacion.getId());

        Call<com.google.gson.JsonObject> call = RetrofitClient.getApiService().iniciarConversacion(token, body);

        call.enqueue(new Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(Call<com.google.gson.JsonObject> call, Response<com.google.gson.JsonObject> response) {
                binding.btnContactarVendedor.setEnabled(true);
                binding.btnContactarVendedor.setText("💬 Contactar vendedor");

                if (response.isSuccessful() && response.body() != null) {
                    com.google.gson.JsonObject data = response.body();

                    if (data.has("conversacion")) {
                        com.google.gson.JsonObject conversacion = data.getAsJsonObject("conversacion");
                        int conversacionId = conversacion.get("id").getAsInt();

                        Toast.makeText(DetallePublicacionActivity.this,
                                "Chat creado", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(DetallePublicacionActivity.this, ConversacionActivity.class);
                        intent.putExtra("conversacionId", conversacionId);
                        intent.putExtra("vendedorNombre", publicacion.getVendedor().getNombre() + " " + publicacion.getVendedor().getApellido());
                        intent.putExtra("vendedorFoto", publicacion.getVendedor().getFotoPerfil());
                        intent.putExtra("tituloLibro", publicacion.getTitulo());
                        intent.putExtra("otroUsuarioId", publicacion.getVendedor().getId()); // ← ANTES del startActivity
                        startActivity(intent); // ← siempre al final
                    }
                } else {
                    Toast.makeText(DetallePublicacionActivity.this,
                            "Error al iniciar chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.google.gson.JsonObject> call, Throwable t) {
                binding.btnContactarVendedor.setEnabled(true);
                binding.btnContactarVendedor.setText("💬 Contactar vendedor");
                Toast.makeText(DetallePublicacionActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}