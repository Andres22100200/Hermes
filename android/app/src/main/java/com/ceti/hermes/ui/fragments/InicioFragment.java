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
import androidx.recyclerview.widget.RecyclerView;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.databinding.FragmentInicioBinding;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.ceti.hermes.ui.publicaciones.PublicacionesAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private PublicacionesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        cargarPublicaciones();
    }

    private void setupRecyclerView() {
        adapter = new PublicacionesAdapter("http://192.168.100.5:3000", publicacion -> {
            // Abrir DetallePublicacionActivity
            Intent intent = new Intent(getContext(), DetallePublicacionActivity.class);
            intent.putExtra("publicacion_id", publicacion.getId());
            startActivity(intent);
        });

        binding.recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerPublicaciones.setAdapter(adapter);
    }

    private void cargarPublicaciones() {
        mostrarLoading(true);

        Call<Map<String, Object>> call = RetrofitClient.getApiService().getPublicaciones(
                null,  // busqueda
                null,  // genero
                null,  // zona
                null,  // precioMin
                null,  // precioMax
                null   // ordenar
        );

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
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
                            "Error al cargar publicaciones",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(getContext(),
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Publicacion> convertirPublicaciones(List<Map<String, Object>> publicacionesMap) {
        List<Publicacion> publicaciones = new ArrayList<>();
        Gson gson = new Gson();

        for (Map<String, Object> map : publicacionesMap) {
            String json = gson.toJson(map);
            Publicacion publicacion = gson.fromJson(json, Publicacion.class);
            publicaciones.add(publicacion);
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
        // Recargar publicaciones cuando volvemos al fragment
        cargarPublicaciones();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}