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
import com.ceti.hermes.data.models.PuntoEncuentro;
import com.ceti.hermes.databinding.ActivityCrearPublicacionBinding;
import com.ceti.hermes.utils.SessionManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearPublicacionActivity extends AppCompatActivity {

    private ActivityCrearPublicacionBinding binding;
    private SessionManager sessionManager;

    private List<Uri> fotosSeleccionadas = new ArrayList<>();
    private List<String> generosSeleccionados = new ArrayList<>();
    private PuntoEncuentro puntoSeleccionado = null;
    private List<PuntoEncuentro> puntosDisponibles = new ArrayList<>();

    private ActivityResultLauncher<String> pickImagesLauncher;

    // Lista de géneros
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

        binding = ActivityCrearPublicacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Inicializar selector de imágenes múltiples
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        if (uris.size() > 5) {
                            Toast.makeText(this, "Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show();
                            fotosSeleccionadas = new ArrayList<>(uris.subList(0, 5));
                        } else {
                            fotosSeleccionadas = new ArrayList<>(uris);
                        }
                        actualizarTextoFotos();
                    }
                }
        );

        setupEstadoDropdown();
        cargarPuntosEncuentro();
        setupListeners();
    }

    private void setupEstadoDropdown() {
        String[] estados = {"Nuevo", "Como nuevo", "Muy bueno", "Bueno", "Aceptable"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                estados
        );
        binding.actvEstado.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSeleccionarFotos.setOnClickListener(v -> seleccionarFotos());
        binding.btnSeleccionarGeneros.setOnClickListener(v -> mostrarDialogGeneros());
        binding.btnSeleccionarPunto.setOnClickListener(v -> mostrarDialogPuntos());
        binding.btnPublicar.setOnClickListener(v -> validarYPublicar());
    }

    private void seleccionarFotos() {
        pickImagesLauncher.launch("image/*");
    }

    private void actualizarTextoFotos() {
        if (fotosSeleccionadas.isEmpty()) {
            binding.tvFotosSeleccionadas.setText("No hay fotos seleccionadas");
        } else {
            binding.tvFotosSeleccionadas.setText(fotosSeleccionadas.size() + " foto(s) seleccionada(s)");
        }
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
            actualizarTextoGeneros();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void actualizarTextoGeneros() {
        if (generosSeleccionados.isEmpty()) {
            binding.tvGenerosSeleccionados.setText("Ninguno seleccionado");
        } else {
            binding.tvGenerosSeleccionados.setText(
                    "Seleccionados (" + generosSeleccionados.size() + "): " +
                            String.join(", ", generosSeleccionados)
            );
        }
    }

    private void cargarPuntosEncuentro() {
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getPuntosEncuentro();

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> puntos = (List<Map<String, Object>>) response.body().get("puntos");
                    if (puntos != null) {
                        for (Map<String, Object> punto : puntos) {
                            PuntoEncuentro p = new PuntoEncuentro();
                            p.setId(((Double) punto.get("id")).intValue());
                            p.setNombre((String) punto.get("nombre"));
                            p.setTipo((String) punto.get("tipo"));
                            p.setZona((String) punto.get("zona"));
                            puntosDisponibles.add(p);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CrearPublicacionActivity.this,
                        "Error al cargar puntos de encuentro",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogPuntos() {
        if (puntosDisponibles.isEmpty()) {
            Toast.makeText(this, "Cargando puntos de encuentro...", Toast.LENGTH_SHORT).show();
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
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void validarYPublicar() {
        // Validar fotos
        if (fotosSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar al menos 1 foto", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar título
        String titulo = binding.etTitulo.getText().toString().trim();
        if (TextUtils.isEmpty(titulo)) {
            binding.tilTitulo.setError("El título es requerido");
            return;
        }
        binding.tilTitulo.setError(null);

        // Validar autor
        String autor = binding.etAutor.getText().toString().trim();
        if (TextUtils.isEmpty(autor)) {
            binding.tilAutor.setError("El autor es requerido");
            return;
        }
        binding.tilAutor.setError(null);

        // Validar géneros
        if (generosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar al menos 1 género", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar estado
        String estado = binding.actvEstado.getText().toString();
        if (TextUtils.isEmpty(estado)) {
            binding.tilEstado.setError("El estado es requerido");
            return;
        }
        binding.tilEstado.setError(null);

        // Validar precio
        String precio = binding.etPrecio.getText().toString().trim();
        if (TextUtils.isEmpty(precio)) {
            binding.tilPrecio.setError("El precio es requerido");
            return;
        }
        binding.tilPrecio.setError(null);

        // Validar punto de encuentro
        if (puntoSeleccionado == null) {
            Toast.makeText(this, "Debes seleccionar un punto de encuentro", Toast.LENGTH_LONG).show();
            return;
        }

        // Todo validado, publicar
        publicarLibro();
    }

    private void publicarLibro() {
        mostrarLoading(true);

        try {
            // Preparar campos de texto
            RequestBody tituloBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etTitulo.getText().toString().trim());
            RequestBody autorBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etAutor.getText().toString().trim());
            RequestBody editorialBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etEditorial.getText().toString().trim());
            RequestBody yearBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etYear.getText().toString().trim());
            RequestBody estadoBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.actvEstado.getText().toString());
            RequestBody precioBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etPrecio.getText().toString().trim());
            RequestBody descripcionBody = RequestBody.create(MediaType.parse("text/plain"),
                    binding.etDescripcion.getText().toString().trim());
            RequestBody puntoBody = RequestBody.create(MediaType.parse("text/plain"),
                    puntoSeleccionado.getNombre());

            // Preparar géneros
            List<RequestBody> generosBody = new ArrayList<>();
            for (String genero : generosSeleccionados) {
                generosBody.add(RequestBody.create(MediaType.parse("text/plain"), genero));
            }

            // Preparar fotos
            List<MultipartBody.Part> fotosParts = new ArrayList<>();
            for (int i = 0; i < fotosSeleccionadas.size(); i++) {
                Uri uri = fotosSeleccionadas.get(i);

                // Leer bytes de la imagen
                InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                inputStream.close();

                // Crear archivo temporal
                File tempFile = new File(getCacheDir(), "temp_book_" + i + ".jpg");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                fos.write(bytes);
                fos.close();

                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
                MultipartBody.Part part = MultipartBody.Part.createFormData("fotos", "book_" + i + ".jpg", requestFile);
                fotosParts.add(part);
            }

            // Llamar a la API
            String token = sessionManager.getBearerToken();
            Call<Map<String, Object>> call = RetrofitClient.getApiService().createPublicacion(
                    token, tituloBody, autorBody, editorialBody, yearBody, generosBody,
                    estadoBody, descripcionBody, precioBody, puntoBody, fotosParts
            );

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    mostrarLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(CrearPublicacionActivity.this,
                                "¡Publicación creada exitosamente!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(CrearPublicacionActivity.this,
                                "Error al crear publicación",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    mostrarLoading(false);
                    Toast.makeText(CrearPublicacionActivity.this,
                            "Error de conexión: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            mostrarLoading(false);
            Toast.makeText(this, "Error al procesar imágenes: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.btnPublicar.setEnabled(!mostrar);
    }
}