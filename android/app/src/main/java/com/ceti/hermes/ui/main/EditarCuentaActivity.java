package com.ceti.hermes.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.databinding.ActivityEditarCuentaBinding;
import com.ceti.hermes.utils.SessionManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarCuentaActivity extends AppCompatActivity {

    private ActivityEditarCuentaBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarCuentaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupToolbar();
        cargarDatosActuales();
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

    private void cargarDatosActuales() {
        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService().getProfile(token);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    Map<String, Object> usuario = (Map<String, Object>) data.get("usuario");
                    if (usuario != null) {
                        String correo = (String) usuario.get("correo");
                        String telefono = (String) usuario.get("numeroTelefonico");
                        String fecha = (String) usuario.get("fechaNacimiento");

                        if (correo != null) binding.etCorreo.setText(correo);
                        if (telefono != null) binding.etTelefono.setText(telefono);
                        if (fecha != null) binding.etFecha.setText(fecha);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(EditarCuentaActivity.this,
                        "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // DatePicker para fecha de nacimiento
        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());
        binding.tilFecha.setEndIconOnClickListener(v -> mostrarDatePicker());

        binding.btnGuardar.setOnClickListener(v -> validarYGuardar());
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Si ya hay una fecha, parsearla
        String fechaActual = binding.etFecha.getText().toString();
        if (!TextUtils.isEmpty(fechaActual)) {
            try {
                String[] partes = fechaActual.split("-");
                calendar.set(Integer.parseInt(partes[0]),
                        Integer.parseInt(partes[1]) - 1,
                        Integer.parseInt(partes[2]));
            } catch (Exception e) {
                // usar fecha actual
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    binding.etFecha.setText(fecha);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Máximo: hace 18 años
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        dialog.show();
    }

    private void validarYGuardar() {
        String correo = binding.etCorreo.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();
        String fecha = binding.etFecha.getText().toString().trim();

        // Validar correo
        if (TextUtils.isEmpty(correo)) {
            binding.tilCorreo.setError("El correo es requerido");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError("Ingresa un correo válido");
            return;
        }
        binding.tilCorreo.setError(null);

        // Validar teléfono
        if (TextUtils.isEmpty(telefono)) {
            binding.tilTelefono.setError("El teléfono es requerido");
            return;
        }
        if (!telefono.matches("\\d{10,15}")) {
            binding.tilTelefono.setError("El teléfono debe tener entre 10 y 15 dígitos");
            return;
        }
        binding.tilTelefono.setError(null);

        guardarCambios(correo, telefono, fecha);
    }

    private void guardarCambios(String correo, String telefono, String fecha) {
        mostrarLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("correo", correo);
        body.put("numeroTelefonico", telefono);
        if (!TextUtils.isEmpty(fecha)) {
            body.put("fechaNacimiento", fecha);
        }

        String token = sessionManager.getBearerToken();
        Call<Map<String, Object>> call = RetrofitClient.getApiService().actualizarCuenta(token, body);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(EditarCuentaActivity.this,
                            "¡Datos actualizados!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String error = response.errorBody() != null
                                ? response.errorBody().string() : "Error desconocido";
                        Toast.makeText(EditarCuentaActivity.this,
                                error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(EditarCuentaActivity.this,
                                "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(EditarCuentaActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.btnGuardar.setEnabled(!mostrar);
    }
}