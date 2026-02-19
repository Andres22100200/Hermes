package com.ceti.hermes.ui.auth.verifyotp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.data.models.LoginResponse;
import com.ceti.hermes.databinding.ActivityVerifyOtpBinding;
import com.ceti.hermes.ui.auth.login.LoginActivity;
import com.ceti.hermes.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    private ActivityVerifyOtpBinding binding;
    private String correo;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar ViewBinding
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Obtener correo del Intent
        correo = getIntent().getStringExtra("correo");

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(this, "Error: correo no recibido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar el correo
        binding.tvEmail.setText(correo);

        // Configurar listeners
        setupListeners();
    }

    private void setupListeners() {
        // Botón de verificar
        binding.btnVerify.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString().trim();

            if (validarOtp(otp)) {
                verificarOtp(otp);
            }
        });

        // Reenviar código
        binding.tvResendCode.setOnClickListener(v -> {
            reenviarOtp();
        });
    }

    private boolean validarOtp(String otp) {
        if (TextUtils.isEmpty(otp)) {
            binding.tilOtp.setError("El código es requerido");
            return false;
        }

        if (otp.length() != 6) {
            binding.tilOtp.setError("El código debe tener 6 dígitos");
            return false;
        }

        binding.tilOtp.setError(null);
        return true;
    }

    private void verificarOtp(String otp) {
        // Mostrar loading
        mostrarLoading(true);

        // Preparar datos
        Map<String, String> otpData = new HashMap<>();
        otpData.put("correo", correo);
        otpData.put("codigoOTP", otp);

        // Llamar a la API
        Call<LoginResponse> call = RetrofitClient.getApiService().verifyOTP(otpData);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse verifyResponse = response.body();

                    // Mostrar mensaje
                    Toast.makeText(VerifyOtpActivity.this,
                            "¡Cuenta verificada exitosamente!",
                            Toast.LENGTH_SHORT).show();

                    // Ir a LoginActivity
                    Intent intent = new Intent(VerifyOtpActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    String errorMsg = "Código OTP inválido o expirado";

                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(VerifyOtpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    binding.etOtp.setText("");
                    binding.etOtp.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(VerifyOtpActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reenviarOtp() {
        // Mostrar loading
        mostrarLoading(true);

        // Preparar datos
        Map<String, String> emailData = new HashMap<>();
        emailData.put("correo", correo);

        // Llamar a la API
        Call<Map<String, String>> call = RetrofitClient.getApiService().resendOTP(emailData);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                mostrarLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Código reenviado exitosamente. Revisa tu correo.",
                            Toast.LENGTH_LONG).show();

                    binding.etOtp.setText("");
                    binding.etOtp.requestFocus();

                } else {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Error al reenviar código",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(VerifyOtpActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnVerify.setEnabled(false);
            binding.tvResendCode.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnVerify.setEnabled(true);
            binding.tvResendCode.setEnabled(true);
        }
    }
}