package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.data.model.Tecnico
class TecnicoRepository {

    suspend fun  geTecnicos(): List<Tecnico>{
        return RetrofitInstance.api.getTecnicos()
    }

    suspend fun GuardarTecnico(nombre:String, cargo:String):Tecnico{
        val nuevo = Tecnico(id=null, nombre = nombre , cargo= cargo)
        return RetrofitInstance.api.guardarTecnico(nuevo)
    }

    suspend fun eliminarTecnico(id:Long){
        RetrofitInstance.api.eliminarTecnico(id)
    }
}