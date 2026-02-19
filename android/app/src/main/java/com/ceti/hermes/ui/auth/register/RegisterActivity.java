package com.ceti.hermes.ui.auth.register;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ceti.hermes.ui.auth.verifyotp.VerifyOtpActivity;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.LoginResponse;
import com.ceti.hermes.data.models.RegisterRequest;
import com.ceti.hermes.databinding.ActivityRegisterBinding;
import com.ceti.hermes.ui.auth.login.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private List<String> generosSeleccionados = new ArrayList<>();

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
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar componentes
        setupSexoDropdown();
        setupListeners();
    }

    private void setupSexoDropdown() {
        String[] opciones = {"Masculino", "Femenino", "Otro", "Prefiero no decir"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
        );
        binding.actvSexo.setAdapter(adapter);
    }

    private void setupListeners() {
        // Selector de fecha
        binding.etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        // Selector de géneros
        binding.btnSeleccionarGeneros.setOnClickListener(v -> mostrarDialogGeneros());

        // Botón de registro
        binding.btnRegister.setOnClickListener(v -> {
            if (validarCampos()) {
                registrarUsuario();
            }
        });

        // Ya tienes cuenta - ir a login
        binding.tvYaTienesCuenta.setOnClickListener(v -> {
            finish(); // Volver a LoginActivity
        });
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18; // Por defecto hace 18 años
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Validar que sea mayor de 18 años
                    Calendar fechaNac = Calendar.getInstance();
                    fechaNac.set(selectedYear, selectedMonth, selectedDay);

                    Calendar hoy = Calendar.getInstance();
                    int edad = hoy.get(Calendar.YEAR) - fechaNac.get(Calendar.YEAR);

                    if (hoy.get(Calendar.MONTH) < fechaNac.get(Calendar.MONTH) ||
                            (hoy.get(Calendar.MONTH) == fechaNac.get(Calendar.MONTH) &&
                                    hoy.get(Calendar.DAY_OF_MONTH) < fechaNac.get(Calendar.DAY_OF_MONTH))) {
                        edad--;
                    }

                    if (edad < 18) {
                        Toast.makeText(this, "Debes ser mayor de 18 años", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Formato: YYYY-MM-DD
                    String fecha = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    binding.etFechaNacimiento.setText(fecha);
                },
                year,
                month,
                day
        );

        // Establecer fecha máxima (18 años atrás)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void mostrarDialogGeneros() {
        boolean[] checkedItems = new boolean[GENEROS_DISPONIBLES.length];

        // Marcar los géneros ya seleccionados
        for (int i = 0; i < GENEROS_DISPONIBLES.length; i++) {
            checkedItems[i] = generosSeleccionados.contains(GENEROS_DISPONIBLES[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona tus géneros favoritos");

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

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            actualizarTextGeneros();
        });

        builder.setNegativeButton("Cancelar", null);

        builder.create().show();
    }

    private void actualizarTextGeneros() {
        if (generosSeleccionados.isEmpty()) {
            binding.tvGenerosSeleccionados.setText("Ninguno seleccionado");
        } else {
            binding.tvGenerosSeleccionados.setText(
                    "Seleccionados (" + generosSeleccionados.size() + "): " +
                            String.join(", ", generosSeleccionados)
            );
        }
    }

    private boolean validarCampos() {
        boolean valido = true;

        // Nombre
        if (TextUtils.isEmpty(binding.etNombre.getText())) {
            binding.tilNombre.setError("El nombre es requerido");
            valido = false;
        } else {
            binding.tilNombre.setError(null);
        }

        // Apellido
        if (TextUtils.isEmpty(binding.etApellido.getText())) {
            binding.tilApellido.setError("El apellido es requerido");
            valido = false;
        } else {
            binding.tilApellido.setError(null);
        }

        // Email
        String email = binding.etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("El correo es requerido");
            valido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            valido = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Teléfono
        String telefono = binding.etTelefono.getText().toString().trim();
        if (TextUtils.isEmpty(telefono)) {
            binding.tilTelefono.setError("El teléfono es requerido");
            valido = false;
        } else if (telefono.length() != 10) {
            binding.tilTelefono.setError("Debe tener 10 dígitos");
            valido = false;
        } else {
            binding.tilTelefono.setError(null);
        }

        // Fecha de nacimiento
        if (TextUtils.isEmpty(binding.etFechaNacimiento.getText())) {
            binding.tilFechaNacimiento.setError("La fecha de nacimiento es requerida");
            valido = false;
        } else {
            binding.tilFechaNacimiento.setError(null);
        }

        // Sexo
        if (TextUtils.isEmpty(binding.actvSexo.getText())) {
            binding.tilSexo.setError("Selecciona tu sexo");
            valido = false;
        } else {
            binding.tilSexo.setError(null);
        }

        // Contraseña
        String password = binding.etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("La contraseña es requerida");
            valido = false;
        } else if (password.length() < 8) {
            binding.tilPassword.setError("Debe tener al menos 8 caracteres");
            valido = false;
        } else {
            binding.tilPassword.setError(null);
        }

        // Géneros
        if (generosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar al menos un género literario", Toast.LENGTH_LONG).show();
            valido = false;
        }

        return valido;
    }

    private void registrarUsuario() {
        // Mostrar loading
        mostrarLoading(true);

        // Preparar datos
        RegisterRequest request = new RegisterRequest(
                binding.etNombre.getText().toString().trim(),
                binding.etApellido.getText().toString().trim(),
                binding.etFechaNacimiento.getText().toString(),
                binding.actvSexo.getText().toString(),
                binding.etTelefono.getText().toString().trim(),
                binding.etEmail.getText().toString().trim(),
                binding.etPassword.getText().toString(),
                generosSeleccionados
        );

        // Llamar a la API
        Call<LoginResponse> call = RetrofitClient.getApiService().register(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse registerResponse = response.body();

                    Toast.makeText(RegisterActivity.this,
                            registerResponse.getMensaje(),
                            Toast.LENGTH_LONG).show();

                    // Ir a pantalla de verificación OTP
                    Intent intent = new Intent(RegisterActivity.this,
                            com.ceti.hermes.ui.auth.verifyotp.VerifyOtpActivity.class);
                    intent.putExtra("correo", binding.etEmail.getText().toString().trim());
                    startActivity(intent);
                    finish();

                } else {
                    String errorMsg = "Error al registrar usuario";

                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
        }
    }
}