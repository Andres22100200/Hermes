package com.ceti.hermes.data.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("token")
    private String token;

    @SerializedName("usuario")
    private User usuario;

    // Constructor vacío
    public LoginResponse() {
    }

    // Getters y Setters
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }
}