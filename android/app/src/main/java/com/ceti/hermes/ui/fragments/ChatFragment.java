package com.ceti.hermes.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ceti.hermes.R;
import com.ceti.hermes.adapters.ConversacionesAdapter;
import com.ceti.hermes.data.models.Conversacion;
import com.ceti.hermes.data.api.ApiService;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerConversaciones;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private ConversacionesAdapter adapter;
    private List<Conversacion> conversacionesList;

    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Inicializar vistas
        recyclerConversaciones = view.findViewById(R.id.recyclerConversaciones);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // Inicializar servicios
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        SessionManager sessionManager = new SessionManager(requireContext());
        token = sessionManager.getBearerToken();

        // Configurar RecyclerView
        conversacionesList = new ArrayList<>();
        adapter = new ConversacionesAdapter(conversacionesList, requireContext());
        recyclerConversaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConversaciones.setAdapter(adapter);

        // Cargar conversaciones
        cargarConversaciones();

        return view;
    }

    private void cargarConversaciones() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerConversaciones.setVisibility(View.GONE);

        apiService.obtenerMisConversaciones(token).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();

                    if (jsonResponse.has("conversaciones")) {
                        JsonArray conversacionesArray = jsonResponse.getAsJsonArray("conversaciones");

                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Conversacion>>(){}.getType();
                        conversacionesList = gson.fromJson(conversacionesArray, listType);

                        if (conversacionesList != null && !conversacionesList.isEmpty()) {
                            adapter.actualizarConversaciones(conversacionesList);
                            recyclerConversaciones.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar conversaciones", Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar conversaciones al volver al fragmento
        cargarConversaciones();
    }
}