package com.ceti.hermes.data.api;

import com.ceti.hermes.data.models.LoginResponse;
import com.ceti.hermes.data.models.RegisterRequest;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

import okhttp3.RequestBody;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface ApiService {

    // ========== AUTENTICACIÓN ==========

    /**
     * Registrar un nuevo usuario
     * POST /api/auth/register
     */
    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    /**
     * Iniciar sesión
     * POST /api/auth/login
     */
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body Map<String, String> credentials);

    /**
     * Verificar código OTP
     * POST /api/auth/verify-otp
     */
    @POST("api/auth/verify-otp")
    Call<LoginResponse> verifyOTP(@Body Map<String, String> otpData);

    /**
     * Reenviar código OTP
     * POST /api/auth/resend-otp
     */
    @POST("api/auth/resend-otp")
    Call<Map<String, String>> resendOTP(@Body Map<String, String> email);

    /**
     * Solicitar recuperación de contraseña
     * POST /api/auth/forgot-password
     */
    @POST("api/auth/forgot-password")
    Call<Map<String, String>> forgotPassword(@Body Map<String, String> email);

    /**
     * Restablecer contraseña
     * POST /api/auth/reset-password
     */
    @POST("api/auth/reset-password")
    Call<Map<String, String>> resetPassword(@Body Map<String, String> resetData);

    /**
     * Obtener perfil del usuario autenticado
     * GET /api/auth/perfil
     */
    @GET("api/auth/perfil")
    Call<Map<String, Object>> getUserProfile(@Header("Authorization") String token);


    /**
     * Obtener perfil público de un usuario
     * GET /api/profile/usuario/:usuarioId
     */
    @GET("api/profile/usuario/{usuarioId}")
    Call<JsonObject> obtenerPerfilPublico(
            @Header("Authorization") String token,
            @Path("usuarioId") int usuarioId
    );

    /**
     * Actualizar biografía
     * PUT /api/profile/biografia
     */
    @PUT("api/profile/biografia")
    Call<Map<String, String>> updateBiografia(
            @Header("Authorization") String token,
            @Body Map<String, String> biografia
    );

    /**
     * Actualizar géneros preferidos
     * PUT /api/profile/generos
     */
    @PUT("api/profile/generos")
    Call<Map<String, Object>> updateGeneros(
            @Header("Authorization") String token,
            @Body Map<String, List<String>> generos
    );

    /**
     * Subir foto de perfil
     * POST /api/profile/foto
     */
    @Multipart
    @POST("api/profile/foto")
    Call<Map<String, String>> uploadProfilePicture(
            @Header("Authorization") String token,
            @Part MultipartBody.Part foto
    );

    // ========== PUBLICACIONES ==========

    /**
     * Obtener catálogo de puntos de encuentro
     * GET /api/publicaciones/puntos-encuentro
     */
    @GET("api/publicaciones/puntos-encuentro")
    Call<Map<String, Object>> getPuntosEncuentro();

    /**
     * Crear publicación
     * POST /api/publicaciones
     */
    @Multipart
    @POST("api/publicaciones")
    Call<Map<String, Object>> createPublicacion(
            @Header("Authorization") String token,
            @Part("titulo") RequestBody titulo,
            @Part("autor") RequestBody autor,
            @Part("editorial") RequestBody editorial,
            @Part("yearPublicacion") RequestBody yearPublicacion,
            @Part("generos") List<RequestBody> generos,
            @Part("estadoLibro") RequestBody estadoLibro,
            @Part("descripcion") RequestBody descripcion,
            @Part("precio") RequestBody precio,
            @Part("puntoEncuentro") RequestBody puntoEncuentro,
            @Part List<MultipartBody.Part> fotos
    );

    /**
     * Obtener todas las publicaciones
     * GET /api/publicaciones
     */
    @GET("api/publicaciones")
    Call<Map<String, Object>> getPublicaciones(
            @Query("busqueda") String busqueda,
            @Query("genero") String genero,
            @Query("zona") String zona,
            @Query("precioMin") String precioMin,
            @Query("precioMax") String precioMax,
            @Query("ordenar") String ordenar
    );

    /**
     * Obtener mis publicaciones
     * GET /api/publicaciones/user/mis-publicaciones
     */
    @GET("api/publicaciones/user/mis-publicaciones")
    Call<Map<String, Object>> getMisPublicaciones(
            @Header("Authorization") String token
    );

    /**
     * Obtener detalle de publicación
     * GET /api/publicaciones/:id
     */
    @GET("api/publicaciones/{id}")
    Call<Map<String, Object>> getPublicacion(@Path("id") int id);

    /**
     * Actualizar publicación
     * PUT /api/publicaciones/:id
     */
    @PUT("api/publicaciones/{id}")
    Call<Map<String, Object>> updatePublicacion(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body Map<String, Object> publicacion
    );

    /**
     * Eliminar publicación
     * DELETE /api/publicaciones/:id
     */
    @DELETE("api/publicaciones/{id}")
    Call<Map<String, Object>> deletePublicacion(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @GET("api/publicaciones/{id}")
    Call<Map<String, Object>> obtenerPublicacion(@Path("id") int id);


    // ============= ENDPOINTS DE CHAT =============

    /**
     * Iniciar conversación
     * POST /api/conversaciones
     */
    @POST("api/conversaciones")
    Call<JsonObject> iniciarConversacion(
            @Header("Authorization") String token,
            @Body JsonObject body
    );

    /**
     * Obtener mis conversaciones
     * GET /api/conversaciones
     */
    @GET("api/conversaciones")
    Call<JsonObject> obtenerMisConversaciones(
            @Header("Authorization") String token
    );

    /**
     * Obtener mensajes de una conversación
     * GET /api/conversaciones/:id/mensajes
     */
    @GET("api/conversaciones/{id}/mensajes")
    Call<JsonObject> obtenerMensajes(
            @Header("Authorization") String token,
            @Path("id") int conversacionId
    );

    /**
     * Enviar mensaje
     * POST /api/conversaciones/mensajes
     */
    @POST("api/conversaciones/mensajes")
    Call<JsonObject> enviarMensaje(
            @Header("Authorization") String token,
            @Body JsonObject body
    );

    /**
     * Eliminar conversación
     * DELETE /api/conversaciones/:id
     */
    @DELETE("api/conversaciones/{id}")
    Call<JsonObject> eliminarConversacion(
            @Header("Authorization") String token,
            @Path("id") int conversacionId
    );

    /**
     * Marcar transacción como completada
     * PUT /api/conversaciones/:id/completar
     */
    @PUT("api/conversaciones/{id}/completar")
    Call<JsonObject> completarTransaccion(
            @Header("Authorization") String token,
            @Path("id") int conversacionId
    );

    // ============= ENDPOINTS DE VALORACIÓN =============

    /**
     * Verificar si puede valorar
     * GET /api/valoraciones/puede-valorar/:conversacionId
     */
    @GET("api/valoraciones/puede-valorar/{conversacionId}")
    Call<JsonObject> puedeValorar(
            @Header("Authorization") String token,
            @Path("conversacionId") int conversacionId
    );

    /**
     * Crear valoración
     * POST /api/valoraciones
     */
    @POST("api/valoraciones")
    Call<JsonObject> crearValoracion(
            @Header("Authorization") String token,
            @Body JsonObject body
    );

    /**
     * Obtener valoraciones de un usuario
     * GET /api/valoraciones/usuario/:usuarioId
     */
    @GET("api/valoraciones/usuario/{usuarioId}")
    Call<JsonObject> obtenerValoracionesUsuario(
            @Header("Authorization") String token,
            @Path("usuarioId") int usuarioId
    );

    // ============= ENDPOINTS DE FAVORITOS =============

    /**
     * Obtener mis favoritos
     * GET /api/favoritos
     */
    @GET("api/favoritos")
    Call<JsonObject> obtenerFavoritos(
            @Header("Authorization") String token
    );

    /**
     * Verificar si es favorito
     * GET /api/favoritos/check/:publicacionId
     */
    @GET("api/favoritos/check/{publicacionId}")
    Call<JsonObject> verificarFavorito(
            @Header("Authorization") String token,
            @Path("publicacionId") int publicacionId
    );

    /**
     * Agregar a favoritos
     * POST /api/favoritos
     */
    @POST("api/favoritos")
    Call<JsonObject> agregarFavorito(
            @Header("Authorization") String token,
            @Body JsonObject body
    );

    /**
     * Quitar de favoritos
     * DELETE /api/favoritos/:publicacionId
     */
    @DELETE("api/favoritos/{publicacionId}")
    Call<JsonObject> quitarFavorito(
            @Header("Authorization") String token,
            @Path("publicacionId") int publicacionId
    );
}