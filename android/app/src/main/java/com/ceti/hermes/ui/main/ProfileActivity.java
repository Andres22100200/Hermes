package com.ceti.hermes.ui.main;

import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.databinding.ActivityProfileBinding;
import com.ceti.hermes.ui.auth.login.LoginActivity;
import com.ceti.hermes.ui.publicaciones.MisPublicacionesAdapter;
import com.ceti.hermes.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.google.gson.Gson;
import java.util.ArrayList;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private MisPublicacionesAdapter misPublicacionesAdapter;
    private List<String> generosActuales = new ArrayList<>();

    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;


    // Lista completa de géneros
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

        // Inicializar ViewBinding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Mostrar imagen seleccionada
                        binding.imgFotoPerfil.setImageURI(uri);
                        // Subir al servidor
                        subirFotoPerfil(uri);
                    }
                }
        );

        // Verificar sesión
        if (!sessionManager.isLoggedIn()) {
            irALogin();
            return;
        }

        // Cargar perfil
        cargarPerfil();

        setupMisPublicaciones();
        cargarMisPublicaciones();

        // Configurar listeners
        setupListeners();
    }

    private void setupListeners() {
        // Editar biografía
        binding.btnEditarBiografia.setOnClickListener(v -> mostrarDialogEditarBiografia());

        // Editar géneros
        binding.btnEditarGeneros.setOnClickListener(v -> mostrarDialogEditarGeneros());

        // Cambiar foto de perfil
        binding.btnCambiarFoto.setOnClickListener(v -> seleccionarImagen());

        // Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarPerfil() {
        mostrarLoading(true);

        // Llamar a la API
        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getUserProfile(token);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    Map<String, Object> usuario = (Map<String, Object>) data.get("usuario");

                    if (usuario != null) {
                        mostrarDatosPerfil(usuario);
                    }

                } else {
                    Toast.makeText(ProfileActivity.this,
                            "Error al cargar perfil",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ProfileActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDatosPerfil(Map<String, Object> usuario) {
        // Nombre completo
        String nombre = (String) usuario.get("nombre");
        String apellido = (String) usuario.get("apellido");
        binding.tvNombreCompleto.setText(nombre + " " + apellido);

        // Correo
        String correo = (String) usuario.get("correo");
        binding.tvCorreo.setText(correo);

        // Biografía
        String biografia = (String) usuario.get("biografia");
        binding.tvBiografia.setText(
                !TextUtils.isEmpty(biografia) ? biografia : "Sin biografía"
        );

        // Géneros - PARSEAR CORRECTAMENTE
        Object generosObj = usuario.get("generosPreferidos");
        generosActuales = new ArrayList<>();

        if (generosObj instanceof List) {
            generosActuales = (List<String>) generosObj;
        } else if (generosObj instanceof String) {
            // Si viene como String JSON, parsearlo manualmente
            String generosJson = (String) generosObj;

            // Remover corchetes y comillas
            generosJson = generosJson.replace("[", "").replace("]", "")
                    .replace("\"", "").replace("\\", "");

            if (!generosJson.trim().isEmpty()) {
                String[] generosArray = generosJson.split(",");
                for (String genero : generosArray) {
                    generosActuales.add(genero.trim());
                }
            }
        }

        binding.tvGeneros.setText(
                !generosActuales.isEmpty() ? String.join(", ", generosActuales) : "Sin géneros"
        );

        // AGREGAR ESTO ↓ - Foto de perfil
        String fotoPerfil = (String) usuario.get("fotoPerfil");
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            // Construir URL completa
            String fotoUrl = "http://192.168.100.5:3000/uploads/profile-pictures/" + fotoPerfil;

            // Cargar con Glide
            Glide.with(ProfileActivity.this)
                    .load(fotoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.imgFotoPerfil);
        } else {
            // Sin foto, usar ícono por defecto
            binding.imgFotoPerfil.setImageResource(android.R.drawable.ic_menu_gallery);
        }



        // Reputación vendedor
        Object promedioVendedor = usuario.get("promedioEstrellas_vendedor");
        Object totalVendedor = usuario.get("totalValoraciones_vendedor");
        String estrellas_vendedor = promedioVendedor != null ? promedioVendedor.toString() : "0.0";
        String total_vendedor = totalVendedor != null ? totalVendedor.toString() : "0";
        binding.tvEstrellasVendedor.setText("⭐ " + estrellas_vendedor + " (" + total_vendedor + " valoraciones)");

        // Reputación comprador
        Object promedioComprador = usuario.get("promedioEstrellas_comprador");
        Object totalComprador = usuario.get("totalValoraciones_comprador");
        String estrellas_comprador = promedioComprador != null ? promedioComprador.toString() : "0.0";
        String total_comprador = totalComprador != null ? totalComprador.toString() : "0";
        binding.tvEstrellasComprador.setText("⭐ " + estrellas_comprador + " (" + total_comprador + " valoraciones)");
    }

    private void mostrarDialogEditarBiografia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Biografía");

        // Crear EditText
        final TextInputEditText input = new TextInputEditText(this);
        input.setText(binding.tvBiografia.getText().toString().equals("Sin biografía")
                ? "" : binding.tvBiografia.getText().toString());
        input.setHint("Escribe tu biografía (máx. 1000 caracteres)");
        input.setMaxLines(5);
        input.setPadding(50, 40, 50, 40);

        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevaBiografia = input.getText().toString().trim();
            actualizarBiografia(nuevaBiografia);
        });

        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    private void actualizarBiografia(String biografia) {
        mostrarLoading(true);

        Map<String, String> data = new HashMap<>();
        data.put("biografia", biografia);

        String token = sessionManager.getBearerToken();
        Call<Map<String, String>> call = RetrofitClient.getApiService().updateBiografia(token, data);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    String nuevaBiografia = response.body().get("biografia");
                    binding.tvBiografia.setText(
                            !TextUtils.isEmpty(nuevaBiografia) ? nuevaBiografia : "Sin biografía"
                    );
                    Toast.makeText(ProfileActivity.this,
                            "Biografía actualizada",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "Error al actualizar biografía",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(ProfileActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogEditarGeneros() {
        boolean[] checkedItems = new boolean[GENEROS_DISPONIBLES.length];

        // Marcar géneros actuales
        for (int i = 0; i < GENEROS_DISPONIBLES.length; i++) {
            checkedItems[i] = generosActuales.contains(GENEROS_DISPONIBLES[i]);
        }

        List<String> generosSeleccionados = new ArrayList<>(generosActuales);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Géneros Favoritos");

        builder.setMultiChoiceItems(GENEROS_DISPONIBLES, checkedItems,
                (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!generosSeleccionados.contains(GENEROS_DISPONIBLES[which])) {
                            generosSeleccionados.add(GENEROS_DISPONIBLES[which]);
                        }
                    } else {
                        generosSeleccionados.remove(GENEROS_DISPONIBLES[which]);
                    }
                });

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            if (generosSeleccionados.isEmpty()) {
                Toast.makeText(this, "Debes seleccionar al menos un género", Toast.LENGTH_SHORT).show();
            } else {
                actualizarGeneros(generosSeleccionados);
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.create().show();
    }

    private void actualizarGeneros(List<String> generos) {
        mostrarLoading(true);

        // DEBUG: Ver qué estamos enviando
        android.util.Log.d("ProfileActivity", "Géneros a enviar: " + generos.toString());

        Map<String, List<String>> data = new HashMap<>();
        data.put("generosPreferidos", generos);

        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService().updateGeneros(token, data);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                mostrarLoading(false);

                // DEBUG: Ver qué respondió el servidor
                android.util.Log.d("ProfileActivity", "Response code: " + response.code());
                android.util.Log.d("ProfileActivity", "Response body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    List<String> nuevosGeneros = (List<String>) response.body().get("generosPreferidos");
                    if (nuevosGeneros != null) {
                        generosActuales = nuevosGeneros;
                        binding.tvGeneros.setText(String.join(", ", nuevosGeneros));
                        Toast.makeText(ProfileActivity.this,
                                "Géneros actualizados",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Mostrar el error
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin error";
                        android.util.Log.e("ProfileActivity", "Error: " + errorBody);
                        Toast.makeText(ProfileActivity.this,
                                "Error: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                android.util.Log.e("ProfileActivity", "Failure: " + t.getMessage());
                Toast.makeText(ProfileActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void irALogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void seleccionarImagen() {
        pickImageLauncher.launch("image/*");
    }

    private void subirFotoPerfil(Uri imageUri) {
        mostrarLoading(true);

        try {
            // Obtener InputStream de la URI
            java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                Toast.makeText(this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                mostrarLoading(false);
                return;
            }

            // Leer bytes de la imagen
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            // Crear archivo temporal
            File tempFile = new File(getCacheDir(), "temp_profile_pic.jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            fos.write(bytes);
            fos.close();

            // Crear RequestBody con tipo MIME correcto
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/jpeg"),
                    tempFile
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "foto",
                    "profile.jpg",
                    requestFile
            );

            // Llamar a la API
            String token = sessionManager.getBearerToken();
            Call<Map<String, String>> call = RetrofitClient.getApiService().uploadProfilePicture(token, body);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    mostrarLoading(false);

                    // DEBUG
                    android.util.Log.d("ProfileActivity", "Response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        String fotoPerfil = response.body().get("fotoPerfil");

                        Toast.makeText(ProfileActivity.this,
                                "Foto actualizada exitosamente",
                                Toast.LENGTH_SHORT).show();

                        // NO es necesario recargar, la foto ya se ve porque usamos setImageURI
                        // Pero guardamos el nombre para futuras cargas

                        // Eliminar archivo temporal
                        tempFile.delete();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin error";
                            android.util.Log.e("ProfileActivity", "Error: " + errorBody);
                            Toast.makeText(ProfileActivity.this,
                                    "Error: " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(ProfileActivity.this,
                                    "Error al subir foto",
                                    Toast.LENGTH_SHORT).show();
                        }
                        tempFile.delete();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    mostrarLoading(false);
                    android.util.Log.e("ProfileActivity", "Failure: " + t.getMessage());
                    Toast.makeText(ProfileActivity.this,
                            "Error de conexión: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    tempFile.delete();
                }
            });

        } catch (Exception e) {
            mostrarLoading(false);
            android.util.Log.e("ProfileActivity", "Exception: " + e.getMessage());
            Toast.makeText(this, "Error al procesar imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cerrarSesion() {
        sessionManager.logout();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, com.ceti.hermes.ui.auth.login.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupMisPublicaciones() {
        misPublicacionesAdapter = new MisPublicacionesAdapter("http://192.168.100.5:3000",
                new MisPublicacionesAdapter.OnPublicacionActionListener() {
                    @Override
                    public void onEditarClick(Publicacion publicacion) {
                        Toast.makeText(ProfileActivity.this,
                                "Editar: " + publicacion.getTitulo(),
                                Toast.LENGTH_SHORT).show();
                        // TODO: Implementar edición
                    }

                    @Override
                    public void onEliminarClick(Publicacion publicacion) {
                        mostrarDialogEliminar(publicacion);
                    }

                    @Override
                    public void onPublicacionClick(Publicacion publicacion) {
                        // Abrir detalle
                        Intent intent = new Intent(ProfileActivity.this, DetallePublicacionActivity.class);
                        intent.putExtra("publicacion_id", publicacion.getId());
                        startActivity(intent);
                    }
                });

        binding.recyclerMisPublicaciones.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerMisPublicaciones.setAdapter(misPublicacionesAdapter);
    }

    private void cargarMisPublicaciones() {
        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getMisPublicaciones(token);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> publicacionesMap =
                            (List<Map<String, Object>>) response.body().get("publicaciones");

                    if (publicacionesMap != null && !publicacionesMap.isEmpty()) {
                        List<Publicacion> publicaciones = convertirPublicaciones(publicacionesMap);
                        misPublicacionesAdapter.setPublicaciones(publicaciones);

                        binding.recyclerMisPublicaciones.setVisibility(View.VISIBLE);
                        binding.tvEmptyPublicaciones.setVisibility(View.GONE);
                    } else {
                        binding.recyclerMisPublicaciones.setVisibility(View.GONE);
                        binding.tvEmptyPublicaciones.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this,
                        "Error al cargar publicaciones",
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

    private void mostrarDialogEliminar(Publicacion publicacion) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar publicación")
                .setMessage("¿Estás seguro de que deseas eliminar \"" + publicacion.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarPublicacion(publicacion))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPublicacion(Publicacion publicacion) {
        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService()
                .deletePublicacion(token, publicacion.getId());

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this,
                            "Publicación eliminada",
                            Toast.LENGTH_SHORT).show();

                    misPublicacionesAdapter.removePublicacion(publicacion.getId());

                    // Si ya no hay publicaciones, mostrar mensaje
                    if (misPublicacionesAdapter.getItemCount() == 0) {
                        binding.recyclerMisPublicaciones.setVisibility(View.GONE);
                        binding.tvEmptyPublicaciones.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "Error al eliminar publicación",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this,
                        "Error de conexión",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}