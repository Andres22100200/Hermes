package com.ceti.hermes.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.databinding.FragmentBuscarBinding;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.ceti.hermes.ui.publicaciones.PublicacionesAdapter;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import retrofit2.http.Path;

public class BuscarFragment extends Fragment {

    private FragmentBuscarBinding binding;
    private PublicacionesAdapter adapter;

    // Filtros activos
    private String busquedaActual = null;
    private List<String> generosActuales = new ArrayList<>();  // MÚLTIPLES géneros
    private String zonaActual = null;
    private String precioMinActual = null;
    private String precioMaxActual = null;
    private String ordenActual = "reciente";

    // Puntos de encuentro cargados desde el backend
    private List<String> puntosDisponibles = new ArrayList<>();

    // Géneros disponibles
    private final String[] GENEROS = {
            "Ficción", "Ciencia ficción", "Fantasía", "Terror", "Misterio/Thriller",
            "Romance", "Aventura", "Histórica", "Distopía", "Realismo mágico",
            "Humor/Sátira", "Juvenil", "Infantil", "No ficción", "Biografía",
            "Memorias", "Ensayo", "Divulgación científica", "Historia",
            "Periodismo literario", "Autoayuda", "Viajes", "Filosofía"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBuscarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();
        cargarPuntosEncuentro();
        cargarHistorial(); // Cargar historial en lugar de todas las publicaciones
    }

    private void setupRecyclerView() {
        adapter = new PublicacionesAdapter("http://192.168.100.5:3000", publicacion -> {
            // Guardar en historial antes de abrir detalle
            guardarEnHistorial(publicacion);

            Intent intent = new Intent(getContext(), DetallePublicacionActivity.class);
            intent.putExtra("publicacion_id", publicacion.getId());
            startActivity(intent);
        });
        binding.recyclerResultados.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerResultados.setAdapter(adapter);
    }

    private void setupListeners() {
        // Búsqueda al presionar Enter
        binding.etBusqueda.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                busquedaActual = binding.etBusqueda.getText().toString().trim();
                buscarPublicaciones();
                return true;
            }
            return false;
        });

        // Botón Filtros
        binding.btnFiltros.setOnClickListener(v -> mostrarDialogFiltros());

        // Botón Limpiar Filtros
        binding.btnLimpiarFiltros.setOnClickListener(v -> limpiarFiltros());
        binding.btnLimpiarHistorial.setOnClickListener(v -> limpiarHistorial());
    }

    private void cargarPuntosEncuentro() {
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getPuntosEncuentro();

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> puntos = (List<Map<String, Object>>) response.body().get("puntos");

                    if (puntos != null) {
                        puntosDisponibles.clear();
                        for (Map<String, Object> punto : puntos) {
                            String nombre = (String) punto.get("nombre");
                            String tipo = (String) punto.get("tipo");
                            puntosDisponibles.add(nombre + " (" + tipo + ")");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Si falla, usar zonas básicas como fallback
                puntosDisponibles.add("Centro");
                puntosDisponibles.add("Norte");
                puntosDisponibles.add("Sur");
                puntosDisponibles.add("Este");
                puntosDisponibles.add("Oeste");
            }
        });
    }

    private void mostrarDialogFiltros() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filtros y Ordenamiento");

        String[] opciones = {
                "Filtrar por Género",
                "Filtrar por Punto de Encuentro",
                "Filtrar por Precio",
                "Ordenar resultados"
        };

        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0:
                    mostrarFiltroGenero();
                    break;
                case 1:
                    mostrarFiltroZona();
                    break;
                case 2:
                    mostrarFiltroPrecio();
                    break;
                case 3:
                    mostrarOrdenamiento();
                    break;
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarFiltroGenero() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Géneros (varios)");

        // Determinar cuáles están seleccionados
        boolean[] seleccionados = new boolean[GENEROS.length];
        for (int i = 0; i < GENEROS.length; i++) {
            seleccionados[i] = generosActuales.contains(GENEROS[i]);
        }

        builder.setMultiChoiceItems(GENEROS, seleccionados, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!generosActuales.contains(GENEROS[which])) {
                    generosActuales.add(GENEROS[which]);
                }
            } else {
                generosActuales.remove(GENEROS[which]);
            }
        });

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            actualizarChipsFiltros();
            buscarPublicaciones();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarFiltroZona() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Punto de Encuentro");

        if (puntosDisponibles.isEmpty()) {
            Toast.makeText(requireContext(), "Cargando puntos de encuentro...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] puntosArray = puntosDisponibles.toArray(new String[0]);

        // Determinar cuál está seleccionado
        int seleccionado = -1;
        for (int i = 0; i < puntosArray.length; i++) {
            String puntoNombre = puntosArray[i].split(" \\(")[0];
            if (puntoNombre.equals(zonaActual)) {
                seleccionado = i;
                break;
            }
        }

        builder.setSingleChoiceItems(puntosArray, seleccionado, (dialog, which) -> {
            String puntoCompleto = puntosArray[which];
            String puntoNombre = puntoCompleto.split(" \\(")[0];
            zonaActual = puntoNombre;
        });

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            actualizarChipsFiltros();
            buscarPublicaciones();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarFiltroPrecio() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rango de Precio");

        // Layout simple con dos EditText
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        android.widget.EditText etMin = new android.widget.EditText(requireContext());
        etMin.setHint("Precio mínimo");
        etMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (precioMinActual != null) etMin.setText(precioMinActual);

        android.widget.EditText etMax = new android.widget.EditText(requireContext());
        etMax.setHint("Precio máximo");
        etMax.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (precioMaxActual != null) etMax.setText(precioMaxActual);

        layout.addView(etMin);
        layout.addView(etMax);

        builder.setView(layout);

        builder.setPositiveButton("Aplicar", (dialog, which) -> {
            String min = etMin.getText().toString().trim();
            String max = etMax.getText().toString().trim();

            precioMinActual = min.isEmpty() ? null : min;
            precioMaxActual = max.isEmpty() ? null : max;

            actualizarChipsFiltros();
            buscarPublicaciones();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarOrdenamiento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ordenar por");

        String[] opciones = {
                "Más recientes",
                "Más antiguos",
                "Precio: menor a mayor",
                "Precio: mayor a menor"
        };

        int seleccionado = 0;
        if (ordenActual.equals("reciente")) seleccionado = 0;
        else if (ordenActual.equals("antiguos")) seleccionado = 1;
        else if (ordenActual.equals("precio_asc")) seleccionado = 2;
        else if (ordenActual.equals("precio_desc")) seleccionado = 3;

        builder.setSingleChoiceItems(opciones, seleccionado, (dialog, which) -> {
            switch (which) {
                case 0: ordenActual = "reciente"; break;
                case 1: ordenActual = "antiguos"; break;
                case 2: ordenActual = "precio_asc"; break;
                case 3: ordenActual = "precio_desc"; break;
            }
            buscarPublicaciones();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void actualizarChipsFiltros() {
        binding.chipGroupFiltros.removeAllViews();

        if (!generosActuales.isEmpty()) {
            for (String genero : generosActuales) {
                agregarChip("Género: " + genero, () -> {
                    generosActuales.remove(genero);
                    actualizarChipsFiltros();
                    buscarPublicaciones();
                });
            }
        }

        if (zonaActual != null) {
            agregarChip("Punto: " + zonaActual, () -> {
                zonaActual = null;
                actualizarChipsFiltros();
                buscarPublicaciones();
            });
        }

        if (precioMinActual != null || precioMaxActual != null) {
            String texto = "Precio: ";
            if (precioMinActual != null) texto += "$" + precioMinActual;
            if (precioMinActual != null && precioMaxActual != null) texto += " - ";
            if (precioMaxActual != null) texto += "$" + precioMaxActual;

            agregarChip(texto, () -> {
                precioMinActual = null;
                precioMaxActual = null;
                actualizarChipsFiltros();
                buscarPublicaciones();
            });
        }

        // Mostrar/ocultar botón limpiar
        boolean hayFiltros = !generosActuales.isEmpty() || zonaActual != null ||
                precioMinActual != null || precioMaxActual != null;
        binding.btnLimpiarFiltros.setVisibility(hayFiltros ? View.VISIBLE : View.GONE);
    }

    private void agregarChip(String texto, Runnable onClose) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onClose.run());
        binding.chipGroupFiltros.addView(chip);
    }

    private void limpiarFiltros() {
        generosActuales.clear();
        zonaActual = null;
        precioMinActual = null;
        precioMaxActual = null;
        binding.etBusqueda.setText("");
        busquedaActual = null;

        actualizarChipsFiltros();
        buscarPublicaciones();
    }

    private void buscarPublicaciones() {
        mostrarLoading(true);

        // Construir parámetros de búsqueda
        Map<String, String> params = new HashMap<>();
        if (busquedaActual != null && !busquedaActual.isEmpty()) params.put("busqueda", busquedaActual);
        // Enviar múltiples géneros separados por coma
        if (!generosActuales.isEmpty()) {
            String generosStr = String.join(",", generosActuales);
            params.put("genero", generosStr);
        }
        if (zonaActual != null) params.put("zona", zonaActual);
        if (precioMinActual != null) params.put("precioMin", precioMinActual);
        if (precioMaxActual != null) params.put("precioMax", precioMaxActual);
        params.put("ordenar", ordenActual);

        // Llamada a la API
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getPublicaciones(
                params.get("busqueda"),
                params.get("genero"),
                params.get("zona"),
                params.get("precioMin"),
                params.get("precioMax"),
                params.get("ordenar")
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

                        binding.recyclerResultados.setVisibility(View.VISIBLE);
                        binding.tvEmpty.setVisibility(View.GONE);
                    } else {
                        adapter.setPublicaciones(new ArrayList<>());
                        binding.recyclerResultados.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
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
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    // ==================== HISTORIAL DE BÚSQUEDA ====================

    private void guardarEnHistorial(Publicacion publicacion) {
        SharedPreferences prefs = requireContext().getSharedPreferences("hermes_prefs", Context.MODE_PRIVATE);

        // Obtener historial actual (solo guardar IDs)
        String historialJson = prefs.getString("historial_busqueda_ids", "[]");

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Integer>>(){}.getType();
            List<Integer> historial = gson.fromJson(historialJson, type);

            if (historial == null) historial = new ArrayList<>();

            // Eliminar si ya existe (para evitar duplicados)
            historial.removeIf(id -> id == publicacion.getId());

            // Agregar al inicio (LIFO)
            historial.add(0, publicacion.getId());

            // Mantener solo las últimas 5
            if (historial.size() > 5) {
                historial = historial.subList(0, 5);
            }

            // Guardar solo IDs
            String nuevoHistorialJson = gson.toJson(historial);
            prefs.edit().putString("historial_busqueda_ids", nuevoHistorialJson).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarHistorial() {
        SharedPreferences prefs = requireContext().getSharedPreferences("hermes_prefs", Context.MODE_PRIVATE);
        String historialJson = prefs.getString("historial_busqueda_ids", "[]");

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Integer>>(){}.getType();
            List<Integer> historialIds = gson.fromJson(historialJson, type);

            if (historialIds != null && !historialIds.isEmpty()) {
                // Cargar las publicaciones desde la API
                cargarPublicacionesPorIds(historialIds);
            } else {
                binding.recyclerResultados.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.tvEmpty.setText("Busca libros por título, autor o usa los filtros");
            }

        } catch (Exception e) {
            e.printStackTrace();
            binding.recyclerResultados.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.tvEmpty.setText("Busca libros por título, autor o usa los filtros");
        }
    }

    private void cargarPublicacionesPorIds(List<Integer> ids) {
        mostrarLoading(true);

        // Crear lista para acumular las publicaciones
        List<Publicacion> publicacionesHistorial = new ArrayList<>();
        final int[] pendientes = {ids.size()};

        for (Integer id : ids) {
            Call<Map<String, Object>> call = RetrofitClient.getApiService().obtenerPublicacion(id);

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> publicacionMap = (Map<String, Object>) response.body().get("publicacion");

                        if (publicacionMap != null) {
                            Gson gson = new Gson();
                            String json = gson.toJson(publicacionMap);
                            Publicacion publicacion = gson.fromJson(json, Publicacion.class);
                            publicacionesHistorial.add(publicacion);
                        }
                    }

                    pendientes[0]--;

                    // Cuando se carguen todas, mostrar
                    if (pendientes[0] == 0) {
                        mostrarLoading(false);
                        if (!publicacionesHistorial.isEmpty()) {
                            adapter.setPublicaciones(publicacionesHistorial);
                            binding.recyclerResultados.setVisibility(View.VISIBLE);
                            binding.tvEmpty.setVisibility(View.GONE);
                        } else {
                            binding.recyclerResultados.setVisibility(View.GONE);
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                            binding.tvEmpty.setText("Busca libros por título, autor o usa los filtros");
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    pendientes[0]--;
                    if (pendientes[0] == 0) {
                        mostrarLoading(false);
                        if (!publicacionesHistorial.isEmpty()) {
                            adapter.setPublicaciones(publicacionesHistorial);
                            binding.recyclerResultados.setVisibility(View.VISIBLE);
                            binding.tvEmpty.setVisibility(View.GONE);
                        } else {
                            binding.recyclerResultados.setVisibility(View.GONE);
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                            binding.tvEmpty.setText("Error al cargar historial");
                        }
                    }
                }
            });
        }
    }

    private void limpiarHistorial() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Limpiar historial")
                .setMessage("¿Deseas eliminar el historial de búsqueda?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences("hermes_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("historial_busqueda_ids", "[]").apply();

                    adapter.setPublicaciones(new ArrayList<>());
                    binding.recyclerResultados.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setText("Busca libros por título, autor o usa los filtros");

                    Toast.makeText(requireContext(), "Historial eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}