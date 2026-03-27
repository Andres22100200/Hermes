package com.ceti.hermes.data.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import java.lang.reflect.Type;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Publicacion {

    @SerializedName("id")
    private int id;

    @SerializedName("usuarioId")
    private int usuarioId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("autor")
    private String autor;

    @SerializedName("editorial")
    private String editorial;

    @SerializedName("yearPublicacion")
    private Integer yearPublicacion;

    @SerializedName("isbn")
    private String isbn;

    @SerializedName("generos")
    @JsonAdapter(GenerosDeserializer.class)
    private List<String> generos;

    @SerializedName("estadoLibro")
    private String estadoLibro;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("precio")
    private String precio;

    @SerializedName("fotos")
    @JsonAdapter(FotosDeserializer.class)
    private List<String> fotos;

    @SerializedName("puntoEncuentro")
    private String puntoEncuentro;

    @SerializedName("estado")
    private String estado;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Relación con vendedor (opcional)
    @SerializedName("vendedor")
    private User vendedor;

    // Coordenadas del punto de encuentro
    @SerializedName("coordenadasPunto")
    private Coordenadas coordenadas;

    // Clase interna para coordenadas
    public static class Coordenadas {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
    }

    public Coordenadas getCoordenadas() { return coordenadas; }
    public void setCoordenadas(Coordenadas coordenadas) { this.coordenadas = coordenadas; }


    // Constructor vacío
    public Publicacion() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public Integer getYearPublicacion() {
        return yearPublicacion;
    }

    public void setYearPublicacion(Integer yearPublicacion) {
        this.yearPublicacion = yearPublicacion;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public List<String> getGeneros() {
        return generos;
    }

    public void setGeneros(List<String> generos) {
        this.generos = generos;
    }

    public String getEstadoLibro() {
        return estadoLibro;
    }

    public void setEstadoLibro(String estadoLibro) {
        this.estadoLibro = estadoLibro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public String getPuntoEncuentro() {
        return puntoEncuentro;
    }

    public void setPuntoEncuentro(String puntoEncuentro) {
        this.puntoEncuentro = puntoEncuentro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getVendedor() {
        return vendedor;
    }

    public void setVendedor(User vendedor) {
        this.vendedor = vendedor;
    }

    // Deserializador personalizado para géneros
    static class GenerosDeserializer implements JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<String> generos = new ArrayList<>();

            if (json.isJsonArray()) {
                // Si ya es array, parsearlo normal
                for (JsonElement element : json.getAsJsonArray()) {
                    generos.add(element.getAsString());
                }
            } else if (json.isJsonPrimitive()) {
                // Si es String JSON, parsearlo manualmente
                String generosStr = json.getAsString();
                generosStr = generosStr.replace("[", "").replace("]", "")
                        .replace("\"", "").replace("\\", "");

                if (!generosStr.trim().isEmpty()) {
                    String[] generosArray = generosStr.split(",");
                    for (String genero : generosArray) {
                        generos.add(genero.trim());
                    }
                }
            }

            return generos;
        }
    }

    // Deserializador personalizado para fotos
    static class FotosDeserializer implements JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<String> fotos = new ArrayList<>();

            if (json.isJsonArray()) {
                // Si ya es array, parsearlo normal
                for (JsonElement element : json.getAsJsonArray()) {
                    fotos.add(element.getAsString());
                }
            } else if (json.isJsonPrimitive()) {
                // Si es String JSON, parsearlo manualmente
                String fotosStr = json.getAsString();
                fotosStr = fotosStr.replace("[", "").replace("]", "")
                        .replace("\"", "").replace("\\", "");

                if (!fotosStr.trim().isEmpty()) {
                    String[] fotosArray = fotosStr.split(",");
                    for (String foto : fotosArray) {
                        fotos.add(foto.trim());
                    }
                }
            }

            return fotos;
        }
    }

}