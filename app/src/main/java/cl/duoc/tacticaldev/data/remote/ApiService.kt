package cl.duoc.tacticaldev.data.remote

import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.model.Tecnico
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // tecnicos
    @GET("api/v1/tecnicos")
    suspend fun getTecnicos(): List<Tecnico>

    @POST("api/v1/tecnicos")
    suspend fun guardarTecnico(@Body tecnico: Tecnico): Tecnico

    @DELETE("api/v1/tecnicos/{id}")
    suspend fun eliminarTecnico(@Path("id") id: Long)

    // usuarios
    @POST("api/v1/usuarios/login")
    suspend fun loginUsuario(@Body usuario: UsuarioEntity): UsuarioEntity

    @POST("api/v1/usuarios/registro")
    suspend fun registrarUsuario(@Body usuario: UsuarioEntity): UsuarioEntity

    @GET("api/v1/usuarios")
    suspend fun getUsuarios(@Query("rol") rol: String? = null): List<UsuarioEntity>

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
}
