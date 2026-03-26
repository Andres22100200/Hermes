package com.ceti.hermes.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ceti.hermes.adapters.MensajesAdapter;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Conversacion;
import com.ceti.hermes.data.models.Mensaje;
import com.ceti.hermes.data.models.User;
import com.ceti.hermes.databinding.ActivityConversacionBinding;
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversacionActivity extends AppCompatActivity {

    private ActivityConversacionBinding binding;
    private MensajesAdapter adapter;
    private SessionManager sessionManager;

    private int conversacionId;
    private int miUsuarioId;
    private Conversacion conversacion;

    private Handler handler = new Handler();
    private Runnable pollingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConversacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        miUsuarioId = sessionManager.getUserId();

        // Obtener conversacionId del Intent
        conversacionId = getIntent().getIntExtra("conversacionId", -1);
        String vendedorNombre = getIntent().getStringExtra("vendedorNombre");
        String vendedorFoto = getIntent().getStringExtra("vendedorFoto");
        String tituloLibro = getIntent().getStringExtra("tituloLibro");


        if (conversacionId == -1) {
            Toast.makeText(this, "Error: conversación inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupListeners();

        mostrarInfoInicial(vendedorNombre, vendedorFoto, tituloLibro);
        cargarMensajes();
        iniciarPolling();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        adapter = new MensajesAdapter(miUsuarioId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerMensajes.setLayoutManager(layoutManager);
        binding.recyclerMensajes.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnEnviar.setOnClickListener(v -> enviarMensaje());
    }

    private void mostrarInfoInicial(String nombre, String foto, String titulo) {
        if (nombre != null) {
            binding.tvNombreOtroUsuario.setText(nombre);
        }
        if (titulo != null) {
            binding.tvTituloLibro.setText(titulo);
        }
        if (foto != null && !foto.isEmpty()) {
            Glide.with(this)
                    .load(RetrofitClient.getProfilePicUrl(foto))
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(binding.imgOtroUsuario);
        }
    }

    private void cargarMensajes() {
        mostrarLoading(true);

        String token = sessionManager.getBearerToken();

        Call<JsonObject> call = RetrofitClient.getApiService().obtenerMensajes(token, conversacionId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    // Obtener mensajes
                    if (data.has("mensajes")) {
                        JsonArray mensajesArray = data.getAsJsonArray("mensajes");
                        List<Mensaje> mensajes = new ArrayList<>();

                        Gson gson = new Gson();
                        for (int i = 0; i < mensajesArray.size(); i++) {
                            Mensaje mensaje = gson.fromJson(mensajesArray.get(i), Mensaje.class);
                            mensajes.add(mensaje);
                        }

                        adapter.setMensajes(mensajes);
                        scrollToBottom();
                    }
                } else {
                    Toast.makeText(ConversacionActivity.this,
                            "Error al cargar mensajes",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ConversacionActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void enviarMensaje() {
        String contenido = binding.etMensaje.getText().toString().trim();

        if (TextUtils.isEmpty(contenido)) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnEnviar.setEnabled(false);

        String token = sessionManager.getBearerToken();

        JsonObject body = new JsonObject();
        body.addProperty("conversacionId", conversacionId);
        body.addProperty("contenido", contenido);
        body.addProperty("tipo", "texto");

        Call<JsonObject> call = RetrofitClient.getApiService().enviarMensaje(token, body);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnEnviar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    binding.etMensaje.setText("");

                    // Agregar mensaje al adapter
                    JsonObject data = response.body();
                    if (data.has("mensaje")) {
                        Gson gson = new Gson();
                        Mensaje nuevoMensaje = gson.fromJson(data.get("data"), Mensaje.class);
                        adapter.agregarMensaje(nuevoMensaje);
                        scrollToBottom();
                    }
                } else {
                    Toast.makeText(ConversacionActivity.this,
                            "Error al enviar mensaje",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnEnviar.setEnabled(true);
                Toast.makeText(ConversacionActivity.this,
                        "Error de conexión",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                cargarMensajes();
                handler.postDelayed(this, 5000); // Consultar cada 5 segundos
            }
        };
        handler.postDelayed(pollingRunnable, 5000);
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            binding.recyclerMensajes.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }
}