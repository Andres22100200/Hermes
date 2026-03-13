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
    private String promedioEstrellas_vendedor;

    @SerializedName("totalValoraciones_vendedor")
    private Integer totalValoraciones_vendedor;

    @SerializedName("promedioEstrellas_comprador")
    private String promedioEstrellas_comprador;

    @SerializedName("totalValoraciones_comprador")
    private Integer totalValoraciones_comprador;

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

    public String getPromedioEstrellas_vendedor() {
        return promedioEstrellas_vendedor;
    }

    public void setPromedioEstrellas_vendedor(String promedioEstrellas_vendedor) {
        this.promedioEstrellas_vendedor = promedioEstrellas_vendedor;
    }

    public Integer getTotalValoraciones_vendedor() {
        return totalValoraciones_vendedor;
    }

    public void setTotalValoraciones_vendedor(Integer totalValoraciones_vendedor) {
        this.totalValoraciones_vendedor = totalValoraciones_vendedor;
    }

    public String getPromedioEstrellas_comprador() {
        return promedioEstrellas_comprador;
    }

    public void setPromedioEstrellas_comprador(String promedioEstrellas_comprador) {
        this.promedioEstrellas_comprador = promedioEstrellas_comprador;
    }

    public Integer getTotalValoraciones_comprador() {
        return totalValoraciones_comprador;
    }

    public void setTotalValoraciones_comprador(Integer totalValoraciones_comprador) {
        this.totalValoraciones_comprador = totalValoraciones_comprador;
    }

    public boolean isVerificadoOTP() {
        return verificadoOTP;
    }

    public void setVerificadoOTP(boolean verificadoOTP) {
        this.verificadoOTP = verificadoOTP;
    }
}