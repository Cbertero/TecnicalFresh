package cl.duoc.tacticaldev.data.model

import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity

// Lo que se ENVÍA al servidor (los nombres deben ser iguales a los DTO de Spring)
data class LoginRequest(val correo: String, val clave: String)

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String,
    val cargo: String
)

// Lo que RESPONDE el servidor (sin claveHash)
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: String,
    val cargo: String
)

// Convierte la respuesta en entidad de Room. El hash local solo sirve para el login sin internet.
fun UsuarioDto.aEntidad(claveHashLocal: String = "") = UsuarioEntity(
    id = id,
    nombre = nombre,
    correo = correo,
    claveHash = claveHashLocal,
    rol = rol,
    cargo = cargo
)