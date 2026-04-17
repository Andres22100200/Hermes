package com.ceti.hermes.ui.auth.forgotpassword;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ceti.hermes.data.api.RetrofitClient;
import com.ceti.hermes.databinding.ActivityForgotPasswordBinding;
import com.ceti.hermes.ui.auth.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private String correoIngresado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnEnviarCodigo.setOnClickListener(v -> enviarCodigo());
        binding.btnRestablecerPassword.setOnClickListener(v -> restablecerPassword());
    }

    private void enviarCodigo() {
        String correo = binding.etCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            binding.tilCorreo.setError("El correo es requerido");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError("Correo inválido");
            return;
        }
        binding.tilCorreo.setError(null);

        mostrarLoading(true);
        correoIngresado = correo;

        Map<String, String> body = new HashMap<>();
        body.put("correo", correo);

        RetrofitClient.getApiService().forgotPassword(body)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {
                        mostrarLoading(false);
                        // Siempre mostrar éxito por seguridad
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Si el correo existe, recibirás un código",
                                Toast.LENGTH_LONG).show();

                        // Mostrar paso 2
                        binding.etCorreo.setEnabled(false);
                        binding.btnEnviarCodigo.setEnabled(false);
                        binding.layoutPaso2.setVisibility(View.VISIBLE);
                        binding.tvSubtitulo.setText(
                                "Ingresa el código que enviamos a " + correoIngresado);
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        mostrarLoading(false);
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void restablecerPassword() {
        String codigo = binding.etCodigo.getText().toString().trim();
        String nuevaPassword = binding.etNuevaPassword.getText().toString().trim();
        String confirmarPassword = binding.etConfirmarPassword.getText().toString().trim();

        if (TextUtils.isEmpty(codigo)) {
            binding.tilCodigo.setError("El código es requerido");
            return;
        }
        binding.tilCodigo.setError(null);

        if (TextUtils.isEmpty(nuevaPassword)) {
            binding.tilNuevaPassword.setError("La contraseña es requerida");
            return;
        }
        if (nuevaPassword.length() < 8) {
            binding.tilNuevaPassword.setError("Mínimo 8 caracteres");
            return;
        }
        binding.tilNuevaPassword.setError(null);

        if (!nuevaPassword.equals(confirmarPassword)) {
            binding.tilConfirmarPassword.setError("Las contraseñas no coinciden");
            return;
        }
        binding.tilConfirmarPassword.setError(null);

        mostrarLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("correo", correoIngresado);
        body.put("tokenRecuperacion", codigo);
        body.put("nuevaPassword", nuevaPassword);

        RetrofitClient.getApiService().resetPassword(body)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {
                        mostrarLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "¡Contraseña restablecida! Ya puedes iniciar sesión",
                                    Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(ForgotPasswordActivity.this,
                                    LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                String error = response.errorBody() != null
                                        ? response.errorBody().string()
                                        : "Error al restablecer";
                                org.json.JSONObject json = new org.json.JSONObject(error);
                                Toast.makeText(ForgotPasswordActivity.this,
                                        json.optString("error", "Error al restablecer"),
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Error al restablecer contraseña",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        mostrarLoading(false);
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.btnEnviarCodigo.setEnabled(!mostrar);
        binding.btnRestablecerPassword.setEnabled(!mostrar);
    }
}