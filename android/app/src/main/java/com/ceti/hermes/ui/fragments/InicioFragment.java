package com.ceti.hermes.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.databinding.FragmentInicioBinding;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.ceti.hermes.ui.publicaciones.PublicacionesAdapter;
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private PublicacionesAdapter adapter;
    private SessionManager sessionManager;

    private static final String PREF_HISTORIAL_GENEROS = "historial_generos";
    private static final int TIEMPO_MINIMO_SEGUNDOS = 30;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        setupRecyclerView();
        cargarFeed();
    }

    private void setupRecyclerView() {
        adapter = new PublicacionesAdapter(publicacion -> {
            // Registrar tiempo de inicio al abrir publicación
            long tiempoInicio = System.currentTimeMillis();

            Intent intent = new Intent(getContext(), DetallePublicacionActivity.class);
            intent.putExtra("publicacion_id", publicacion.getId());

            // Pasar géneros y tiempo inicio para tracking
            if (publicacion.getGeneros() != null && !publicacion.getGeneros().isEmpty()) {
                intent.putExtra("generos_publicacion",
                        String.join(",", publicacion.getGeneros()));
            }
            intent.putExtra("tiempo_inicio", tiempoInicio);

            startActivity(intent);
        });

        binding.recyclerPublicaciones.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.recyclerPublicaciones.setAdapter(adapter);
    }

    private void cargarFeed() {
        mostrarLoading(true);

        String token = sessionManager.getBearerToken();
        String historialJson = obtenerHistorialGenerosJson();

        Call<Map<String, Object>> call = RetrofitClient.getApiService()
                .getFeed(token, historialJson);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {
                if (binding == null) return;
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> publicacionesMap =
                            (List<Map<String, Object>>) response.body().get("publicaciones");

                    if (publicacionesMap != null && !publicacionesMap.isEmpty()) {
                        List<Publicacion> publicaciones = convertirPublicaciones(publicacionesMap);
                        adapter.setPublicaciones(publicaciones);
                        binding.recyclerPublicaciones.setVisibility(View.VISIBLE);
                        binding.tvEmpty.setVisibility(View.GONE);
                    } else {
                        binding.recyclerPublicaciones.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Error al cargar publicaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (binding == null) return;
                mostrarLoading(false);
                Toast.makeText(getContext(),
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Registrar géneros de una publicación vista más de 30 segundos
    public void registrarTiempoPublicacion(String generosStr, long tiempoInicio) {
        if (generosStr == null || generosStr.isEmpty()) return;

        long tiempoFin = System.currentTimeMillis();
        long segundos = (tiempoFin - tiempoInicio) / 1000;

        if (segundos >= TIEMPO_MINIMO_SEGUNDOS) {
            String[] generos = generosStr.split(",");
            Set<String> historial = obtenerHistorialGeneros();
            for (String genero : generos) {
                historial.add(genero.trim());
            }
            guardarHistorialGeneros(historial);
        }
    }

    private Set<String> obtenerHistorialGeneros() {
        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("hermes_prefs", android.content.Context.MODE_PRIVATE);
        String json = prefs.getString(PREF_HISTORIAL_GENEROS, "[]");
        Set<String> generos = new HashSet<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                generos.add(arr.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generos;
    }

    private void guardarHistorialGeneros(Set<String> generos) {
        JSONArray arr = new JSONArray();
        for (String g : generos) arr.put(g);

        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("hermes_prefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_HISTORIAL_GENEROS, arr.toString()).apply();
    }

    private String obtenerHistorialGenerosJson() {
        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("hermes_prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString(PREF_HISTORIAL_GENEROS, "[]");
    }

    private List<Publicacion> convertirPublicaciones(List<Map<String, Object>> publicacionesMap) {
        List<Publicacion> publicaciones = new ArrayList<>();
        Gson gson = new Gson();
        for (Map<String, Object> map : publicacionesMap) {
            String json = gson.toJson(map);
            publicaciones.add(gson.fromJson(json, Publicacion.class));
        }
        return publicaciones;
    }

    private void mostrarLoading(boolean mostrar) {
        if (binding != null) {
            binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFeed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}