package cl.duoc.tacticaldev.data.model

data class Tecnico(
    val id: Long? = null,
    val nombre: String,
    val cargo: String,
    val localAsignado: String? = "",
    val sincronizado: Boolean = true
)
