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
import com.ceti.hermes.ui.auth.forgotpassword.ForgotPasswordActivity;
import com.ceti.hermes.ui.auth.register.RegisterActivity;
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

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupListeners();

        // Mostrar mensaje de baneo si viene del interceptor
        String mensajeBaneo = getIntent().getStringExtra("mensaje_baneo");
        if (mensajeBaneo != null) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cuenta suspendida")
                    .setMessage(mensajeBaneo)
                    .setPositiveButton("Entendido", null)
                    .show();
        }
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            if (validarCampos(email, password)) {
                login(email, password);
            }
        });

        binding.btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private boolean validarCampos(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("El correo es requerido");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            return false;
        }
        binding.tilEmail.setError(null);

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
        mostrarLoading(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("correo", email);
        credentials.put("password", password);

        Call<LoginResponse> call = RetrofitClient.getApiService().login(credentials);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    sessionManager.saveSession(
                            loginResponse.getToken(),
                            loginResponse.getUsuario().getId(),
                            loginResponse.getUsuario().getCorreo(),
                            loginResponse.getUsuario().getNombre()
                    );

                    Toast.makeText(LoginActivity.this,
                            "¡Bienvenido " + loginResponse.getUsuario().getNombre() + "!",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    String errorMsg = "Correo o contraseña incorrectos";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            org.json.JSONObject json = new org.json.JSONObject(errorBody);
                            String error = json.optString("error", "");

                            if (error.contains("suspendida temporalmente")) {
                                String hasta = json.optString("suspendidoHasta", "");
                                String motivo = json.optString("motivo", "Sin motivo especificado");
                                errorMsg = "Tu cuenta está suspendida temporalmente.\nMotivo: " + motivo;
                                if (!hasta.isEmpty()) {
                                    try {
                                        java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                        java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat(
                                                "dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                                        java.util.Date fecha = sdfIn.parse(hasta);
                                        errorMsg += "\nHasta: " + sdfOut.format(fecha);
                                    } catch (Exception ex) {
                                        errorMsg += "\nHasta: " + hasta;
                                    }
                                }
                            } else if (error.equals("cuenta_eliminada")) {
                                errorMsg = "Tu cuenta ha sido eliminada permanentemente.";
                            } else if (error.equals("cuenta_suspendida")) {
                                errorMsg = "Tu cuenta está suspendida temporalmente.";
                            } else if (!error.isEmpty()) {
                                errorMsg = error;
                            }
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