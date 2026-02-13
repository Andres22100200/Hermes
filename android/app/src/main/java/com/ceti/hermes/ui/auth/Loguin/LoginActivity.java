package com.ceti.hermes.ui.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.MainActivity;
import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.LoginResponse;
import com.ceti.hermes.databinding.ActivityLoginBinding;
import com.ceti.hermes.ui.auth.Register.RegisterActivity;
import com.ceti.hermes.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Configurar listeners
        setupListeners();
    }

    private void setupListeners() {
        // Botón de Login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validarCampos(email, password)) {
                login(email, password);
            }
        });

        // Ir a Registro
        binding.btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Olvidaste tu contraseña (próximamente)
        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validarCampos(String email, String password) {
        // Validar email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("El correo es requerido");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            return false;
        }

        binding.tilEmail.setError(null);

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("La contraseña es requerida");
            return false;
        }

        if (password.length() < 8) {
            binding.tilPassword.setError("La contraseña debe tener al menos 8 caracteres");
            return false;
        }

        binding.tilPassword.setError(null);

        return true;
    }

    private void login(String email, String password) {
        // Mostrar loading
        mostrarLoading(true);

        // Preparar datos
        Map<String, String> credentials = new HashMap<>();
        credentials.put("correo", email);
        credentials.put("password", password);

        // Llamar a la API
        Call<LoginResponse> call = RetrofitClient.getApiService().login(credentials);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Guardar sesión
                    sessionManager.saveSession(
                            loginResponse.getToken(),
                            loginResponse.getUsuario().getId(),
                            loginResponse.getUsuario().getCorreo(),
                            loginResponse.getUsuario().getNombre()
                    );

                    // Mostrar mensaje
                    Toast.makeText(LoginActivity.this,
                            "¡Bienvenido " + loginResponse.getUsuario().getNombre() + "!",
                            Toast.LENGTH_SHORT).show();

                    // Ir a MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // Error del servidor
                    String errorMsg = "Error al iniciar sesión";

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            // Aquí podrías parsear el error JSON si quieres
                            errorMsg = "Correo o contraseña incorrectos";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.btnGoToRegister.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.btnGoToRegister.setEnabled(true);
        }
    }
}