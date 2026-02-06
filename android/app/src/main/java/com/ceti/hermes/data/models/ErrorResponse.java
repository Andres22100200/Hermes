package com.ceti.hermes.data.models;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("error")
    private String error;

    @SerializedName("detalle")
    private String detalle;

    // Constructor vacío
    public ErrorResponse() {
    }

    // Getters y Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}