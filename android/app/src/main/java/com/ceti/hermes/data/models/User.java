package com.ceti.hermes.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("correo")
    private String correo;

    @SerializedName("numeroTelefonico")
    private String numeroTelefonico;

    @SerializedName("fechaNacimiento")
    private String fechaNacimiento;

    @SerializedName("sexo")
    private String sexo;

    @SerializedName("fotoPerfil")
    private String fotoPerfil;

    @SerializedName("biografia")
    private String biografia;

    @SerializedName("generosPreferidos")
    private List<String> generosPreferidos;

    @SerializedName("promedioEstrellas_vendedor")
    private float promedioEstrellasVendedor;

    @SerializedName("promedioEstrellas_comprador")
    private float promedioEstrellasComprador;

    @SerializedName("verificadoOTP")
    private boolean verificadoOTP;

    // Constructor vacío
    public User() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNumeroTelefonico() {
        return numeroTelefonico;
    }

    public void setNumeroTelefonico(String numeroTelefonico) {
        this.numeroTelefonico = numeroTelefonico;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public List<String> getGenerosPreferidos() {
        return generosPreferidos;
    }

    public void setGenerosPreferidos(List<String> generosPreferidos) {
        this.generosPreferidos = generosPreferidos;
    }

    public float getPromedioEstrellasVendedor() {
        return promedioEstrellasVendedor;
    }

    public void setPromedioEstrellasVendedor(float promedioEstrellasVendedor) {
        this.promedioEstrellasVendedor = promedioEstrellasVendedor;
    }

    public float getPromedioEstrellasComprador() {
        return promedioEstrellasComprador;
    }

    public void setPromedioEstrellasComprador(float promedioEstrellasComprador) {
        this.promedioEstrellasComprador = promedioEstrellasComprador;
    }

    public boolean isVerificadoOTP() {
        return verificadoOTP;
    }

    public void setVerificadoOTP(boolean verificadoOTP) {
        this.verificadoOTP = verificadoOTP;
    }
}