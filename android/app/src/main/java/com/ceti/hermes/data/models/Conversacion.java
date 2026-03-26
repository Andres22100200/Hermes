package com.ceti.hermes.data.models;

import com.ceti.hermes.data.models.Publicacion;
import com.ceti.hermes.data.models.User;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Conversacion {

    @SerializedName("id")
    private int id;

    @SerializedName("publicacionId")
    private int publicacionId;

    @SerializedName("compradorId")
    private int compradorId;

    @SerializedName("vendedorId")
    private int vendedorId;

    @SerializedName("completada")
    private boolean completada;

    @SerializedName("eliminadaPorComprador")
    private boolean eliminadaPorComprador;

    @SerializedName("eliminadaPorVendedor")
    private boolean eliminadaPorVendedor;

    @SerializedName("ultimoMensaje")
    private String ultimoMensaje;

    @SerializedName("ultimoMensajeFecha")
    private Date ultimoMensajeFecha;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Relaciones
    @SerializedName("publicacion")
    private Publicacion publicacion;

    @SerializedName("comprador")
    private User comprador;

    @SerializedName("vendedor")
    private User vendedor;

    // Constructor vacío
    public Conversacion() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPublicacionId() {
        return publicacionId;
    }

    public void setPublicacionId(int publicacionId) {
        this.publicacionId = publicacionId;
    }

    public int getCompradorId() {
        return compradorId;
    }

    public void setCompradorId(int compradorId) {
        this.compradorId = compradorId;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(int vendedorId) {
        this.vendedorId = vendedorId;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public boolean isEliminadaPorComprador() {
        return eliminadaPorComprador;
    }

    public void setEliminadaPorComprador(boolean eliminadaPorComprador) {
        this.eliminadaPorComprador = eliminadaPorComprador;
    }

    public boolean isEliminadaPorVendedor() {
        return eliminadaPorVendedor;
    }

    public void setEliminadaPorVendedor(boolean eliminadaPorVendedor) {
        this.eliminadaPorVendedor = eliminadaPorVendedor;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public Date getUltimoMensajeFecha() {
        return ultimoMensajeFecha;
    }

    public void setUltimoMensajeFecha(Date ultimoMensajeFecha) {
        this.ultimoMensajeFecha = ultimoMensajeFecha;
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

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

    public User getComprador() {
        return comprador;
    }

    public void setComprador(User comprador) {
        this.comprador = comprador;
    }

    public User getVendedor() {
        return vendedor;
    }

    public void setVendedor(User vendedor) {
        this.vendedor = vendedor;
    }
}