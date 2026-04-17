package com.ceti.hermes.data.api;

import android.content.Context;
import android.content.Intent;

import com.ceti.hermes.utils.SessionManager;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.100.11:3000/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static Context appContext = null;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static String getProfilePicUrl(String fotoPerfil) {
        if (fotoPerfil == null || fotoPerfil.isEmpty()) return null;
        return BASE_URL + "uploads/profile-pictures/" + fotoPerfil;
    }

    public static String getBookPicUrl(String foto) {
        if (foto == null || foto.isEmpty()) return null;
        return BASE_URL + "uploads/book-pictures/" + foto;
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Response response = chain.proceed(chain.request());

                        if (response.code() == 403 && appContext != null) {
                            try {
                                String bodyStr = response.peekBody(1024).string();
                                JSONObject json = new JSONObject(bodyStr);
                                String error = json.optString("error", "");

                                if (error.equals("cuenta_eliminada") || error.equals("cuenta_suspendida")) {
                                    String mensaje = json.optString("mensaje", "Tu cuenta ha sido suspendida");

                                    SessionManager sessionManager = new SessionManager(appContext);
                                    sessionManager.logout();

                                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    handler.post(() -> {
                                        Intent intent = new Intent(appContext,
                                                com.ceti.hermes.ui.auth.login.LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra("mensaje_baneo", mensaje);
                                        appContext.startActivity(intent);
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
}