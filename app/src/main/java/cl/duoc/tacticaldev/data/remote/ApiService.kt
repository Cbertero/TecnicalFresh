package cl.duoc.tacticaldev.data.remote

import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.model.Tecnico
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import cl.duoc.tacticaldev.data.model.LoginRequest
import cl.duoc.tacticaldev.data.model.RegistroRequest
import cl.duoc.tacticaldev.data.model.UsuarioDto
import cl.duoc.tacticaldev.data.model.ValidacionRequest
import cl.duoc.tacticaldev.data.model.VisitaRequest
import cl.duoc.tacticaldev.model.VisitaDetalle
import retrofit2.http.PUT


interface ApiService {

    // tecnicos
    @GET("api/v1/tecnicos")
    suspend fun getTecnicos(): List<Tecnico>

    @POST("api/v1/tecnicos")
    suspend fun guardarTecnico(@Body tecnico: Tecnico): Tecnico

    @DELETE("api/v1/tecnicos/{id}")
    suspend fun eliminarTecnico(@Path("id") id: Long)

    // usuarios
    // usuarios
    @POST("api/v1/usuarios/login")
    suspend fun loginUsuario(@Body request: LoginRequest): UsuarioDto

    @POST("api/v1/usuarios/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): UsuarioDto

    @GET("api/v1/usuarios")
    suspend fun getUsuarios(@Query("rol") rol: String? = null): List<UsuarioDto>
    // puntos de venta
    @GET("api/v1/puntos-venta")
    suspend fun getPuntosVenta(): List<PuntoVentaEntity>

    @POST("api/v1/puntos-venta")
    suspend fun guardarPuntoVenta(@Body puntoVenta: PuntoVentaEntity): PuntoVentaEntity

    // activos
    @GET("api/v1/activos")
    suspend fun getActivos(): List<ActivoEntity>

    @GET("api/v1/activos/{id}")
    suspend fun getActivoPorId(@Path("id") id: Long): ActivoEntity

    @GET("api/v1/activos/codigo/{codigo}")
    suspend fun getActivoPorCodigo(@Path("codigo") codigo: String): ActivoEntity

    @POST("api/v1/activos")
    suspend fun guardarActivo(@Body activo: ActivoEntity): ActivoEntity

    // visitas
    @POST("api/v1/visitas")
    suspend fun registrarVisita(@Body visita: VisitaRequest): VisitaDetalle

    @GET("api/v1/visitas")
    suspend fun getVisitas(
        @Query("usuarioId") usuarioId: Long? = null,
        @Query("activoId") activoId: Long? = null,
        @Query("estado") estado: String? = null
    ): List<VisitaDetalle>

    @GET("api/v1/visitas/{id}")
    suspend fun getVisita(@Path("id") id: Long): VisitaDetalle

    @PUT("api/v1/visitas/{id}/validar")
    suspend fun validarVisita(@Path("id") id: Long, @Body body: ValidacionRequest): VisitaDetalle

    @PUT("api/v1/visitas/{id}/rechazar")
    suspend fun rechazarVisita(@Path("id") id: Long, @Body body: ValidacionRequest): VisitaDetalle
}
