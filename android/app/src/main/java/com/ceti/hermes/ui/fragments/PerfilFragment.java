package com.ceti.hermes.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.databinding.FragmentPerfilBinding;
import com.ceti.hermes.ui.auth.login.LoginActivity;
import com.ceti.hermes.ui.main.EditarCuentaActivity;
import com.ceti.hermes.ui.publicaciones.DetallePublicacionActivity;
import com.ceti.hermes.ui.publicaciones.EditarPublicacionActivity;
import com.ceti.hermes.ui.publicaciones.MisPublicacionesAdapter;
import com.ceti.hermes.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
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
import com.ceti.hermes.R;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private SessionManager sessionManager;
    private MisPublicacionesAdapter misPublicacionesAdapter;
    private List<String> generosActuales = new ArrayList<>();
    private ActivityResultLauncher<String> pickImageLauncher;

    private final String[] GENEROS_DISPONIBLES = {
            "Ficción", "Ciencia ficción", "Fantasía", "Terror", "Misterio/Thriller",
            "Romance", "Aventura", "Histórica", "Distopía", "Realismo mágico",
            "Humor/Sátira", "Juvenil", "Infantil", "No ficción", "Biografía",
            "Memorias", "Ensayo", "Divulgación científica", "Historia",
            "Periodismo literario", "Autoayuda", "Viajes", "Filosofía"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        binding.imgFotoPerfil.setImageURI(uri);
                        subirFotoPerfil(uri);
                    }
                });

        setupMisPublicaciones();
        setupListeners();
        // Configurar toolbar
        binding.toolbar.inflateMenu(R.menu.menu_perfil);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_editar_cuenta) {
                Intent intent = new Intent(requireContext(), EditarCuentaActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.action_cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return false;
        });



        cargarPerfil();
        cargarMisPublicaciones();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPerfil();
        cargarMisPublicaciones();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupListeners() {
        binding.btnEditarBiografia.setOnClickListener(v -> mostrarDialogEditarBiografia());
        binding.btnEditarGeneros.setOnClickListener(v -> mostrarDialogEditarGeneros());
        binding.btnCambiarFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void cargarPerfil() {
        mostrarLoading(true);
        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getUserProfile(token);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (binding == null) return;
                mostrarLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    Map<String, Object> usuario = (Map<String, Object>) data.get("usuario");
                    if (usuario != null) mostrarDatosPerfil(usuario);
                } else {
                    Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (binding == null) return;
                mostrarLoading(false);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDatosPerfil(Map<String, Object> usuario) {
        String nombre = (String) usuario.get("nombre");
        String apellido = (String) usuario.get("apellido");
        binding.tvNombreCompleto.setText(nombre + " " + apellido);
        binding.tvCorreo.setText((String) usuario.get("correo"));

        String biografia = (String) usuario.get("biografia");
        binding.tvBiografia.setText(!TextUtils.isEmpty(biografia) ? biografia : "Sin biografía");

        Object generosObj = usuario.get("generosPreferidos");
        generosActuales = new ArrayList<>();
        if (generosObj instanceof List) {
            generosActuales = (List<String>) generosObj;
        } else if (generosObj instanceof String) {
            String generosJson = ((String) generosObj)
                    .replace("[", "").replace("]", "")
                    .replace("\"", "").replace("\\", "");
            if (!generosJson.trim().isEmpty()) {
                for (String genero : generosJson.split(",")) {
                    generosActuales.add(genero.trim());
                }
            }
        }
        binding.tvGeneros.setText(!generosActuales.isEmpty()
                ? String.join(", ", generosActuales) : "Sin géneros");

        String fotoPerfil = (String) usuario.get("fotoPerfil");
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(this)
                    .load(RetrofitClient.getProfilePicUrl(fotoPerfil))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.imgFotoPerfil);
        }

        Object promedioV = usuario.get("promedioEstrellas_vendedor");
        Object totalV = usuario.get("totalValoraciones_vendedor");
        binding.tvEstrellasVendedor.setText("⭐ " + (promedioV != null ? promedioV : "0.0")
                + " (" + (totalV != null ? totalV : "0") + " valoraciones)");

        Object promedioC = usuario.get("promedioEstrellas_comprador");
        Object totalC = usuario.get("totalValoraciones_comprador");
        binding.tvEstrellasComprador.setText("⭐ " + (promedioC != null ? promedioC : "0.0")
                + " (" + (totalC != null ? totalC : "0") + " valoraciones)");
    }

    private void mostrarDialogEditarBiografia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar Biografía");

        final TextInputEditText input = new TextInputEditText(requireContext());
        input.setText(binding.tvBiografia.getText().toString().equals("Sin biografía")
                ? "" : binding.tvBiografia.getText().toString());
        input.setHint("Escribe tu biografía (máx. 1000 caracteres)");
        input.setMaxLines(5);
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) ->
                actualizarBiografia(input.getText().toString().trim()));
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void actualizarBiografia(String biografia) {
        mostrarLoading(true);
        Map<String, String> data = new HashMap<>();
        data.put("biografia", biografia);

        RetrofitClient.getApiService().updateBiografia(sessionManager.getBearerToken(), data)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (binding == null) return;
                        mostrarLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            String nueva = response.body().get("biografia");
                            binding.tvBiografia.setText(!TextUtils.isEmpty(nueva) ? nueva : "Sin biografía");
                            Toast.makeText(requireContext(), "Biografía actualizada", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        if (binding == null) return;
                        mostrarLoading(false);
                    }
                });
    }

    private void mostrarDialogEditarGeneros() {
        boolean[] checkedItems = new boolean[GENEROS_DISPONIBLES.length];
        for (int i = 0; i < GENEROS_DISPONIBLES.length; i++) {
            checkedItems[i] = generosActuales.contains(GENEROS_DISPONIBLES[i]);
        }
        List<String> generosSeleccionados = new ArrayList<>(generosActuales);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar Géneros Favoritos");
        builder.setMultiChoiceItems(GENEROS_DISPONIBLES, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!generosSeleccionados.contains(GENEROS_DISPONIBLES[which]))
                    generosSeleccionados.add(GENEROS_DISPONIBLES[which]);
            } else {
                generosSeleccionados.remove(GENEROS_DISPONIBLES[which]);
            }
        });
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            if (generosSeleccionados.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona al menos un género", Toast.LENGTH_SHORT).show();
            } else {
                actualizarGeneros(generosSeleccionados);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void actualizarGeneros(List<String> generos) {
        mostrarLoading(true);
        Map<String, List<String>> data = new HashMap<>();
        data.put("generosPreferidos", generos);

        RetrofitClient.getApiService().updateGeneros(sessionManager.getBearerToken(), data)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (binding == null) return;
                        mostrarLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> nuevos = (List<String>) response.body().get("generosPreferidos");
                            if (nuevos != null) {
                                generosActuales = nuevos;
                                binding.tvGeneros.setText(String.join(", ", nuevos));
                                Toast.makeText(requireContext(), "Géneros actualizados", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (binding == null) return;
                        mostrarLoading(false);
                    }
                });
    }

    private void subirFotoPerfil(Uri imageUri) {
        mostrarLoading(true);
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            File tempFile = new File(requireContext().getCacheDir(), "temp_profile_pic.jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            fos.write(bytes);
            fos.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("foto", "profile.jpg", requestFile);

            RetrofitClient.getApiService().uploadProfilePicture(sessionManager.getBearerToken(), body)
                    .enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (binding == null) return;
                            mostrarLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
                            }
                            tempFile.delete();
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            if (binding == null) return;
                            mostrarLoading(false);
                            tempFile.delete();
                        }
                    });
        } catch (Exception e) {
            mostrarLoading(false);
        }
    }

    private void cerrarSesion() {
        sessionManager.logout();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupMisPublicaciones() {
        misPublicacionesAdapter = new MisPublicacionesAdapter(
                new MisPublicacionesAdapter.OnPublicacionActionListener() {
                    @Override
                    public void onEditarClick(Publicacion publicacion) {
                        Intent intent = new Intent(requireContext(), EditarPublicacionActivity.class);
                        intent.putExtra("publicacion_id", publicacion.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onEliminarClick(Publicacion publicacion) {
                        mostrarDialogEliminar(publicacion);
                    }

                    @Override
                    public void onDesactivarClick(Publicacion publicacion) {
                        mostrarDialogDesactivar(publicacion);
                    }

                    @Override
                    public void onPublicacionClick(Publicacion publicacion) {
                        Intent intent = new Intent(requireContext(), DetallePublicacionActivity.class);
                        intent.putExtra("publicacion_id", publicacion.getId());
                        startActivity(intent);
                    }
                });

        binding.recyclerMisPublicaciones.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerMisPublicaciones.setAdapter(misPublicacionesAdapter);
    }

    private void cargarMisPublicaciones() {
        if (binding == null) return;
        String token = sessionManager.getBearerToken();
        RetrofitClient.getApiService().getMisPublicaciones(token)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> publicacionesMap =
                                    (List<Map<String, Object>>) response.body().get("publicaciones");
                            if (publicacionesMap != null && !publicacionesMap.isEmpty()) {
                                List<Publicacion> publicaciones = new ArrayList<>();
                                Gson gson = new Gson();
                                for (Map<String, Object> map : publicacionesMap) {
                                    publicaciones.add(gson.fromJson(gson.toJson(map), Publicacion.class));
                                }
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
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
    }

    private void mostrarDialogEliminar(Publicacion publicacion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar publicación")
                .setMessage("¿Eliminar \"" + publicacion.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarPublicacion(publicacion))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogDesactivar(Publicacion publicacion) {
        boolean estaDisponible = "Disponible".equals(publicacion.getEstado());
        String nuevoEstado = estaDisponible ? "Inactivo" : "Disponible";

        new AlertDialog.Builder(requireContext())
                .setTitle((estaDisponible ? "Desactivar" : "Activar") + " publicación")
                .setMessage("¿Deseas " + (estaDisponible ? "desactivar" : "activar")
                        + " \"" + publicacion.getTitulo() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> cambiarEstado(publicacion, nuevoEstado))
                .setNegativeButton("No", null)
                .show();
    }

    private void cambiarEstado(Publicacion publicacion, String nuevoEstado) {
        Map<String, String> body = new HashMap<>();
        body.put("estado", nuevoEstado);

        RetrofitClient.getApiService()
                .cambiarEstadoPublicacion(sessionManager.getBearerToken(), publicacion.getId(), body)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful()) {
                            misPublicacionesAdapter.actualizarEstado(publicacion.getId(), nuevoEstado);
                            Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
    }

    private void eliminarPublicacion(Publicacion publicacion) {
        RetrofitClient.getApiService()
                .deletePublicacion(sessionManager.getBearerToken(), publicacion.getId())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (binding == null) return;
                        if (response.isSuccessful()) {
                            misPublicacionesAdapter.removePublicacion(publicacion.getId());
                            Toast.makeText(requireContext(), "Publicación eliminada", Toast.LENGTH_SHORT).show();
                            if (misPublicacionesAdapter.getItemCount() == 0) {
                                binding.recyclerMisPublicaciones.setVisibility(View.GONE);
                                binding.tvEmptyPublicaciones.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                });
    }

    private void mostrarLoading(boolean mostrar) {
        if (binding == null) return;
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }
}