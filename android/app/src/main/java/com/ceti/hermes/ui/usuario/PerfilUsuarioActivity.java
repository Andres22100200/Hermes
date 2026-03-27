package com.ceti.hermes.ui.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ceti.hermes.R;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.databinding.ActivityPerfilUsuarioBinding;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.ceti.hermes.ui.publicaciones.PublicacionesAdapter;
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private ActivityPerfilUsuarioBinding binding;
    private SessionManager sessionManager;
    private PublicacionesAdapter publicacionesAdapter;

    private int usuarioId;
    private int conversacionId = -1; // -1 si no viene desde el chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPerfilUsuarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Obtener datos del Intent
        usuarioId = getIntent().getIntExtra("usuarioId", -1);
        conversacionId = getIntent().getIntExtra("conversacionId", -1);

        if (usuarioId == -1) {
            Toast.makeText(this, "Error: usuario inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        cargarPerfil();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        publicacionesAdapter = new PublicacionesAdapter(publicacion -> {
            Intent intent = new Intent(this, DetallePublicacionActivity.class);
            intent.putExtra("publicacion_id", publicacion.getId());
            startActivity(intent);
        });
        binding.recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPublicaciones.setAdapter(publicacionesAdapter);
    }

    private void cargarPerfil() {
        mostrarLoading(true);

        String token = sessionManager.getBearerToken();

        Call<JsonObject> call = RetrofitClient.getApiService().obtenerPerfilPublico(token, usuarioId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    mostrarDatosPerfil(data);
                } else {
                    Toast.makeText(PerfilUsuarioActivity.this,
                            "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(PerfilUsuarioActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarDatosPerfil(JsonObject data) {
        Gson gson = new Gson();

        // Datos del usuario
        JsonObject usuario = data.getAsJsonObject("usuario");

        String nombre = usuario.get("nombre").getAsString();
        String apellido = usuario.get("apellido").getAsString();
        binding.tvNombreCompleto.setText(nombre + " " + apellido);

        // Biografía
        if (usuario.has("biografia") && !usuario.get("biografia").isJsonNull()) {
            binding.tvBiografia.setText(usuario.get("biografia").getAsString());
        } else {
            binding.tvBiografia.setText("Sin biografía");
        }

        // Foto de perfil
        if (usuario.has("fotoPerfil") && !usuario.get("fotoPerfil").isJsonNull()) {
            String fotoUrl = RetrofitClient.getProfilePicUrl(usuario.get("fotoPerfil").getAsString());
            Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(binding.imgFotoPerfil);
        }

        // Reputación vendedor
        String promedioVendedor = usuario.get("promedioEstrellas_vendedor").getAsString();
        int totalVendedor = usuario.get("totalValoraciones_vendedor").getAsInt();
        binding.tvEstrellasVendedor.setText("⭐ " + promedioVendedor + " (" + totalVendedor + " valoraciones como vendedor)");

        // Reputación comprador
        String promedioComprador = usuario.get("promedioEstrellas_comprador").getAsString();
        int totalComprador = usuario.get("totalValoraciones_comprador").getAsInt();
        binding.tvEstrellasComprador.setText("⭐ " + promedioComprador + " (" + totalComprador + " valoraciones como comprador)");

        // Publicaciones
        if (data.has("publicaciones")) {
            JsonArray publicacionesArray = data.getAsJsonArray("publicaciones");
            List<Publicacion> publicaciones = new ArrayList<>();

            for (int i = 0; i < publicacionesArray.size(); i++) {
                String json = gson.toJson(publicacionesArray.get(i));
                Publicacion publicacion = gson.fromJson(json, Publicacion.class);
                publicaciones.add(publicacion);
            }

            if (!publicaciones.isEmpty()) {
                publicacionesAdapter.setPublicaciones(publicaciones);
                binding.recyclerPublicaciones.setVisibility(View.VISIBLE);
                binding.tvEmptyPublicaciones.setVisibility(View.GONE);
            } else {
                binding.recyclerPublicaciones.setVisibility(View.GONE);
                binding.tvEmptyPublicaciones.setVisibility(View.VISIBLE);
            }
        }

        // Botón valorar — solo si viene desde una conversación
        if (conversacionId != -1) {
            verificarSiPuedeValorar();
        } else {
            binding.btnValorar.setVisibility(View.GONE);
        }

        // Ocultar botón valorar si es mi propio perfil
        if (usuarioId == sessionManager.getUserId()) {
            binding.btnValorar.setVisibility(View.GONE);
        }

        // Listener botón valorar
        binding.btnValorar.setOnClickListener(v -> mostrarDialogValorar());
    }

    private void verificarSiPuedeValorar() {
        String token = sessionManager.getBearerToken();

        Call<JsonObject> call = RetrofitClient.getApiService().puedeValorar(token, conversacionId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    boolean puede = data.get("puede").getAsBoolean();
                    binding.btnValorar.setVisibility(puede ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnValorar.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarDialogValorar() {
        String token = sessionManager.getBearerToken();

        // Primero verificar el rol
        Call<JsonObject> call = RetrofitClient.getApiService().puedeValorar(token, conversacionId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    if (data.get("puede").getAsBoolean()) {
                        String rolEmisor = data.get("rolEmisor").getAsString();
                        mostrarDialogConRol(rolEmisor);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PerfilUsuarioActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogConRol(String rolEmisor) {
        String[] etiquetas;
        if ("comprador".equals(rolEmisor)) {
            etiquetas = new String[]{
                    "Calidad del libro",
                    "Veracidad de la publicación",
                    "Comunicación efectiva",
                    "Puntualidad",
                    "Negociación justa",
                    "Honestidad"
            };
        } else {
            etiquetas = new String[]{
                    "Puntualidad",
                    "Seriedad",
                    "Negociación justa",
                    "Honestidad"
            };
        }

        boolean[] seleccionadas = new boolean[etiquetas.length];
        List<String> etiquetasSeleccionadas = new ArrayList<>();
        final int[] estrellasSeleccionadas = {0}; // 0 = ninguna seleccionada

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Valorar transacción");

        // Layout principal
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        // TextView indicador
        android.widget.TextView tvEstrellas = new android.widget.TextView(this);
        tvEstrellas.setText("Selecciona una calificación");
        tvEstrellas.setTextSize(14);
        tvEstrellas.setPadding(0, 0, 0, 12);
        tvEstrellas.setGravity(android.view.Gravity.CENTER);
        layout.addView(tvEstrellas);

        // Layout horizontal para las 5 estrellas
        android.widget.LinearLayout layoutEstrellas = new android.widget.LinearLayout(this);
        layoutEstrellas.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layoutEstrellas.setGravity(android.view.Gravity.CENTER);
        layoutEstrellas.setPadding(0, 0, 0, 16);

        android.widget.TextView[] estrellas = new android.widget.TextView[5];

        for (int i = 0; i < 5; i++) {
            final int indice = i;
            estrellas[i] = new android.widget.TextView(this);
            estrellas[i].setText("☆");
            estrellas[i].setTextSize(36);
            estrellas[i].setPadding(8, 0, 8, 0);
            estrellas[i].setTextColor(android.graphics.Color.parseColor("#FFC107"));

            estrellas[i].setOnClickListener(v -> {
                estrellasSeleccionadas[0] = indice + 1;
                // Actualizar visual
                for (int j = 0; j < 5; j++) {
                    if (j <= indice) {
                        estrellas[j].setText("★");
                    } else {
                        estrellas[j].setText("☆");
                    }
                }
                tvEstrellas.setText("Calificación: " + estrellasSeleccionadas[0] + "/5");
            });

            layoutEstrellas.addView(estrellas[i]);
        }

        layout.addView(layoutEstrellas);
        builder.setView(layout);

        builder.setMultiChoiceItems(etiquetas, seleccionadas, (dialog, which, isChecked) -> {
            if (isChecked) {
                etiquetasSeleccionadas.add(etiquetas[which]);
            } else {
                etiquetasSeleccionadas.remove(etiquetas[which]);
            }
        });

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            if (estrellasSeleccionadas[0] == 0) {
                Toast.makeText(this, "Selecciona al menos 1 estrella", Toast.LENGTH_SHORT).show();
                return;
            }
            enviarValoracion(estrellasSeleccionadas[0], etiquetasSeleccionadas);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void enviarValoracion(int estrellas, List<String> etiquetas) {
        String token = sessionManager.getBearerToken();

        JsonObject body = new JsonObject();
        body.addProperty("conversacionId", conversacionId);
        body.addProperty("estrellas", estrellas);

        com.google.gson.JsonArray etiquetasArray = new com.google.gson.JsonArray();
        for (String etiqueta : etiquetas) {
            etiquetasArray.add(etiqueta);
        }
        body.add("etiquetas", etiquetasArray);

        Call<JsonObject> call = RetrofitClient.getApiService().crearValoracion(token, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PerfilUsuarioActivity.this,
                            "¡Valoración enviada!", Toast.LENGTH_SHORT).show();
                    binding.btnValorar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(PerfilUsuarioActivity.this,
                            "Error al enviar valoración", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PerfilUsuarioActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}