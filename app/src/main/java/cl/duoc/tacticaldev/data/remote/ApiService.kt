package cl.duoc.tacticaldev.data.remote
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import cl.duoc.tacticaldev.data.model.Tecnico
import retrofit2.http.Body

interface  ApiService{

    @GET("api/v1/tecnicos")
    suspend fun getTecnicos(): List<Tecnico>

    @POST("api/v1/tecnicos")
    suspend fun guardarTecnico(@Body tecnico: Tecnico): Tecnico

    @DELETE("api/v1/tecnicos/{id}")
    suspend fun eliminarTecnico(@Path("id") id: Long)
}