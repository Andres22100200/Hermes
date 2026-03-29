package com.ceti.hermes.ui.fragments;

import android.content.Intent;
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
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.ceti.hermes.ui.publicaciones.PublicacionesAdapter;
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritosFragment extends Fragment {

    private RecyclerView recyclerFavoritos;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private PublicacionesAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favoritos, container, false);

        recyclerFavoritos = view.findViewById(R.id.recyclerFavoritos);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        sessionManager = new SessionManager(requireContext());

        adapter = new PublicacionesAdapter(publicacion -> {
            Intent intent = new Intent(getContext(), DetallePublicacionActivity.class);
            intent.putExtra("publicacion_id", publicacion.getId());
            startActivity(intent);
        });

        recyclerFavoritos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFavoritos.setAdapter(adapter);

        cargarFavoritos();

        return view;
    }

    private void cargarFavoritos() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerFavoritos.setVisibility(View.GONE);

        String token = sessionManager.getBearerToken();

        Call<JsonObject> call = RetrofitClient.getApiService().obtenerFavoritos(token);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    JsonArray favoritosArray = data.getAsJsonArray("favoritos");

                    List<Publicacion> publicaciones = new ArrayList<>();
                    Gson gson = new Gson();

                    for (int i = 0; i < favoritosArray.size(); i++) {
                        JsonObject favoritoObj = favoritosArray.get(i).getAsJsonObject();
                        if (favoritoObj.has("publicacion") && !favoritoObj.get("publicacion").isJsonNull()) {
                            JsonObject pubObj = favoritoObj.getAsJsonObject("publicacion");
                            Publicacion publicacion = gson.fromJson(pubObj, Publicacion.class);
                            publicaciones.add(publicacion);
                        }
                    }

                    if (!publicaciones.isEmpty()) {
                        adapter.setPublicaciones(publicaciones);
                        recyclerFavoritos.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No tienes publicaciones guardadas");
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error al cargar favoritos");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFavoritos();
    }
}