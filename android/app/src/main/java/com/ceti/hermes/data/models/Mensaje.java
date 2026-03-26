package com.ceti.hermes.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Mensaje {

    @SerializedName("id")
    private int id;

    @SerializedName("conversacionId")
    private int conversacionId;

    @SerializedName("remitenteId")
    private int remitenteId;

    @SerializedName("tipo")
    private String tipo; // "texto", "imagen", "sistema"

    @SerializedName("contenido")
    private String contenido;

    @SerializedName("archivo")
    private String archivo;

    @SerializedName("leido")
    private boolean leido;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Relación
    @SerializedName("remitente")
    private User remitente;

    // Constructor vacío
    public Mensaje() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversacionId() {
        return conversacionId;
    }

    public void setConversacionId(int conversacionId) {
        this.conversacionId = conversacionId;
    }

    public int getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(int remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getRemitente() {
        return remitente;
    }

    public void setRemitente(User remitente) {
        this.remitente = remitente;
    }
}