package com.ceti.hermes.data.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.100.11:3000/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    /**
     * Construir URL completa para foto de perfil
     */
    public static String getProfilePicUrl(String fotoPerfil) {
        if (fotoPerfil == null || fotoPerfil.isEmpty()) return null;
        return BASE_URL + "uploads/profile-pictures/" + fotoPerfil;
    }

    /**
     * Construir URL completa para foto de libro
     */
    public static String getBookPicUrl(String foto) {
        if (foto == null || foto.isEmpty()) return null;
        return BASE_URL + "uploads/book-pictures/" + foto;
    }

    /**
     * Obtener instancia de Retrofit (Singleton)
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Obtener instancia de ApiService (Singleton)
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
}