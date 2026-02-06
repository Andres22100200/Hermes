package com.ceti.hermes.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "HermesSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Guardar sesión del usuario
     */
    public void saveSession(String token, int userId, String email, String name) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Obtener token JWT
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Obtener token con formato Bearer
     */
    public String getBearerToken() {
        String token = getToken();
        if (token != null) {
            return "Bearer " + token;
        }
        return null;
    }

    /**
     * Verificar si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtener ID del usuario
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Obtener email del usuario
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Obtener nombre del usuario
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Cerrar sesión (eliminar todos los datos)
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}