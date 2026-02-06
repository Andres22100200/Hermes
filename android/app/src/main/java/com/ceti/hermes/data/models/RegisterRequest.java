package com.ceti.hermes.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RegisterRequest {
    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("fechaNacimiento")
    private String fechaNacimiento;

    @SerializedName("sexo")
    private String sexo;

    @SerializedName("numeroTelefonico")
    private String numeroTelefonico;

    @SerializedName("correo")
    private String correo;

    @SerializedName("password")
    private String password;

    @SerializedName("generosPreferidos")
    private List<String> generosPreferidos;

    // Constructor
    public RegisterRequest(String nombre, String apellido, String fechaNacimiento,
                           String sexo, String numeroTelefonico, String correo,
                           String password, List<String> generosPreferidos) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.numeroTelefonico = numeroTelefonico;
        this.correo = correo;
        this.password = password;
        this.generosPreferidos = generosPreferidos;
    }

    // Getters y Setters
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

    public String getNumeroTelefonico() {
        return numeroTelefonico;
    }

    public void setNumeroTelefonico(String numeroTelefonico) {
        this.numeroTelefonico = numeroTelefonico;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getGenerosPreferidos() {
        return generosPreferidos;
    }

    public void setGenerosPreferidos(List<String> generosPreferidos) {
        this.generosPreferidos = generosPreferidos;
    }
}