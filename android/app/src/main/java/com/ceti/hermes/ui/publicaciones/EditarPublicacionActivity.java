package com.ceti.hermes.ui.publicaciones;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.data.models.PuntoEncuentro;
import com.ceti.hermes.databinding.ActivityEditarPublicacionBinding;
import com.ceti.hermes.utils.SessionManager;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPublicacionActivity extends AppCompatActivity {

    private ActivityEditarPublicacionBinding binding;
    private SessionManager sessionManager;
    private Publicacion publicacion;

    private List<Uri> fotosNuevas = new ArrayList<>();
    private List<String> generosSeleccionados = new ArrayList<>();
    private PuntoEncuentro puntoSeleccionado = null;
    private List<PuntoEncuentro> puntosDisponibles = new ArrayList<>();

    private ActivityResultLauncher<String> pickImagesLauncher;

    private final String[] GENEROS_DISPONIBLES = {
            "Ficción", "Ciencia ficción", "Fantasía", "Terror", "Misterio/Thriller",
            "Romance", "Aventura", "Histórica", "Distopía", "Realismo mágico",
            "Humor/Sátira", "Juvenil", "Infantil", "No ficción", "Biografía",
            "Memorias", "Ensayo", "Divulgación científica", "Historia",
            "Periodismo literario", "Autoayuda", "Viajes", "Filosofía"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarPublicacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        int publicacionId = getIntent().getIntExtra("publicacion_id", -1);
        if (publicacionId == -1) {
            Toast.makeText(this, "Error: publicación inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupEstadoDropdown();
        setupImagePicker();
        cargarPuntosEncuentro();
        cargarPublicacion(publicacionId);
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupEstadoDropdown() {
        String[] estados = {"Nuevo", "Como nuevo", "Muy bueno", "Bueno", "Aceptable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, estados);
        binding.actvEstado.setAdapter(adapter);
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        if (uris.size() > 5) {
                            fotosNuevas = new ArrayList<>(uris.subList(0, 5));
                        } else {
                            fotosNuevas = new ArrayList<>(uris);
                        }
                        binding.tvFotosSeleccionadas.setText(
                                fotosNuevas.size() + " foto(s) nueva(s) seleccionada(s)");
                    }
                });
    }

    private void setupListeners() {
        binding.btnSeleccionarFotos.setOnClickListener(v ->
                pickImagesLauncher.launch("image/*"));

        binding.btnSeleccionarGeneros.setOnClickListener(v ->
                mostrarDialogGeneros());

        binding.btnSeleccionarPunto.setOnClickListener(v ->
                mostrarDialogPuntos());

        binding.btnActualizar.setOnClickListener(v ->
                validarYActualizar());
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
                    Map<String, Object> pubMap = (Map<String, Object>) data.get("publicacion");
                    if (pubMap != null) {
                        Gson gson = new Gson();
                        String json = gson.toJson(pubMap);
                        publicacion = gson.fromJson(json, Publicacion.class);
                        llenarFormulario();
                    }
                } else {
                    Toast.makeText(EditarPublicacionActivity.this,
                            "Error al cargar publicación", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(EditarPublicacionActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void llenarFormulario() {
        binding.etTitulo.setText(publicacion.getTitulo());
        binding.etAutor.setText(publicacion.getAutor());

        if (!TextUtils.isEmpty(publicacion.getEditorial())) {
            binding.etEditorial.setText(publicacion.getEditorial());
        }
        if (publicacion.getYearPublicacion() != null) {
            binding.etYear.setText(String.valueOf(publicacion.getYearPublicacion()));
        }
        if (!TextUtils.isEmpty(publicacion.getDescripcion())) {
            binding.etDescripcion.setText(publicacion.getDescripcion());
        }

        binding.etPrecio.setText(publicacion.getPrecio());
        binding.actvEstado.setText(publicacion.getEstadoLibro(), false);

        // Géneros
        if (publicacion.getGeneros() != null) {
            generosSeleccionados = new ArrayList<>(publicacion.getGeneros());
            binding.tvGenerosSeleccionados.setText(
                    "Seleccionados (" + generosSeleccionados.size() + "): " +
                            String.join(", ", generosSeleccionados));
        }

        // Punto de encuentro
        binding.tvPuntoSeleccionado.setText(publicacion.getPuntoEncuentro());
        binding.tvFotosSeleccionadas.setText("Fotos actuales: " +
                (publicacion.getFotos() != null ? publicacion.getFotos().size() : 0));
    }

    private void cargarPuntosEncuentro() {
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getPuntosEncuentro();

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> puntos =
                            (List<Map<String, Object>>) response.body().get("puntos");
                    if (puntos != null) {
                        Gson gson = new Gson();
                        for (Map<String, Object> puntoMap : puntos) {
                            String json = gson.toJson(puntoMap);
                            PuntoEncuentro p = gson.fromJson(json, PuntoEncuentro.class);
                            puntosDisponibles.add(p);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void mostrarDialogGeneros() {
        boolean[] checkedItems = new boolean[GENEROS_DISPONIBLES.length];
        for (int i = 0; i < GENEROS_DISPONIBLES.length; i++) {
            checkedItems[i] = generosSeleccionados.contains(GENEROS_DISPONIBLES[i]);
        }

        List<String> tempSeleccionados = new ArrayList<>(generosSeleccionados);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona géneros (máximo 3)");
        builder.setMultiChoiceItems(GENEROS_DISPONIBLES, checkedItems,
                (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (tempSeleccionados.size() < 3) {
                            tempSeleccionados.add(GENEROS_DISPONIBLES[which]);
                        } else {
                            Toast.makeText(this, "Máximo 3 géneros", Toast.LENGTH_SHORT).show();
                            ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                        }
                    } else {
                        tempSeleccionados.remove(GENEROS_DISPONIBLES[which]);
                    }
                });

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            generosSeleccionados = tempSeleccionados;
            binding.tvGenerosSeleccionados.setText(
                    "Seleccionados (" + generosSeleccionados.size() + "): " +
                            String.join(", ", generosSeleccionados));
        });
        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void mostrarDialogPuntos() {
        if (puntosDisponibles.isEmpty()) {
            Toast.makeText(this, "Cargando puntos...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombresPuntos = new String[puntosDisponibles.size()];
        for (int i = 0; i < puntosDisponibles.size(); i++) {
            PuntoEncuentro p = puntosDisponibles.get(i);
            nombresPuntos[i] = p.getNombre() + " (" + p.getTipo() + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona punto de encuentro");
        builder.setItems(nombresPuntos, (dialog, which) -> {
            puntoSeleccionado = puntosDisponibles.get(which);
            binding.tvPuntoSeleccionado.setText(puntoSeleccionado.getNombre());
            mostrarMapaPunto(puntoSeleccionado);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void mostrarMapaPunto(PuntoEncuentro punto) {
        if (punto.getCoordenadas() == null) return;

        double lat = punto.getCoordenadas().getLat();
        double lng = punto.getCoordenadas().getLng();

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

        binding.webViewMapa.getSettings().setJavaScriptEnabled(true);
        binding.webViewMapa.getSettings().setDomStorageEnabled(true);
        binding.webViewMapa.setVisibility(View.VISIBLE);
        binding.webViewMapa.loadDataWithBaseURL("https://unpkg.com", html, "text/html", "UTF-8", null);
    }

    private void validarYActualizar() {
        String titulo = binding.etTitulo.getText().toString().trim();
        if (TextUtils.isEmpty(titulo)) {
            binding.tilTitulo.setError("El título es requerido");
            return;
        }
        binding.tilTitulo.setError(null);

        String autor = binding.etAutor.getText().toString().trim();
        if (TextUtils.isEmpty(autor)) {
            binding.tilAutor.setError("El autor es requerido");
            return;
        }
        binding.tilAutor.setError(null);

        if (generosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos 1 género", Toast.LENGTH_SHORT).show();
            return;
        }

        String estado = binding.actvEstado.getText().toString();
        if (TextUtils.isEmpty(estado)) {
            binding.tilEstado.setError("El estado es requerido");
            return;
        }

        String precio = binding.etPrecio.getText().toString().trim();
        if (TextUtils.isEmpty(precio)) {
            binding.tilPrecio.setError("El precio es requerido");
            return;
        }

        actualizarPublicacion();
    }

    private void actualizarPublicacion() {
        mostrarLoading(true);

        try {
            String token = sessionManager.getBearerToken();

            RequestBody tituloBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etTitulo.getText().toString().trim());
            RequestBody autorBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etAutor.getText().toString().trim());
            RequestBody editorialBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etEditorial.getText().toString().trim());
            RequestBody estadoBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.actvEstado.getText().toString());
            RequestBody precioBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etPrecio.getText().toString().trim());
            RequestBody descripcionBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etDescripcion.getText().toString().trim());

            String yearStr = binding.etYear.getText().toString().trim();
            RequestBody yearBody = RequestBody.create(MediaType.parse("text/plain"),
                    TextUtils.isEmpty(yearStr) ? "" : yearStr);

            String puntoNombre = puntoSeleccionado != null
                    ? puntoSeleccionado.getNombre()
                    : publicacion.getPuntoEncuentro();
            RequestBody puntoBody = RequestBody.create(MediaType.parse("text/plain"), puntoNombre);

            // Géneros
            List<MultipartBody.Part> generosParts = new ArrayList<>();
            for (String genero : generosSeleccionados) {
                generosParts.add(MultipartBody.Part.createFormData("generos", genero));
            }

            // Fotos — solo si se seleccionaron nuevas
            List<MultipartBody.Part> fotosParts = new ArrayList<>();
            if (!fotosNuevas.isEmpty()) {
                for (int i = 0; i < fotosNuevas.size(); i++) {
                    Uri uri = fotosNuevas.get(i);
                    java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    inputStream.close();

                    File tempFile = new File(getCacheDir(), "temp_book_edit_" + i + ".jpg");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                    fos.write(bytes);
                    fos.close();

                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
                    fotosParts.add(MultipartBody.Part.createFormData("fotos", "book_" + i + ".jpg", requestFile));
                }
            }

            Call<Map<String, Object>> call = RetrofitClient.getApiService().actualizarPublicacion(
                    token, publicacion.getId(),
                    tituloBody, autorBody, editorialBody, yearBody,
                    estadoBody, descripcionBody, precioBody, puntoBody,
                    fotosParts, generosParts
            );

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    mostrarLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(EditarPublicacionActivity.this,
                                "¡Publicación actualizada!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditarPublicacionActivity.this,
                                "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    mostrarLoading(false);
                    Toast.makeText(EditarPublicacionActivity.this,
                            "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            mostrarLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.btnActualizar.setEnabled(!mostrar);
    }
}