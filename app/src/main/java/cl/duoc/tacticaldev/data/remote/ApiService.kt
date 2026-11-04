package cl.duoc.tacticaldev.data.remote
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import cl.duoc.tacticaldev.data.model.Tecnico
import retrofit2.http.Body

interface  ApiService{

    @GET("/api/vi/tecnicos")
    suspend fun getTecnicos(): List<Tecnico>

    @POST("/api/vi/tecnicos")
    suspend fun guardarTecnico(tecnico: Tecnico): Tecnico

    @DELETE("/api/vi/tecnicos/{id}")
    suspend fun eliminarTecnico(@Path("id") id: Long)
}