package cl.duoc.tacticaldev.model

/**
 * Catálogos del dominio. En Room se guardan con su `name` (String) y en la
 * interfaz se muestra su `etiqueta`.
 */
enum class Rol(val etiqueta: String) {
    OPERARIO("Operario"),
    SUPERVISOR("Supervisor")
}

enum class TipoActivo(val etiqueta: String) {
    VITRINA_FREEZER("Vitrina freezer"),
    CONSERVADORA("Conservadora")
}

enum class TipoVisita(val etiqueta: String) {
    MANTENCION("Mantención preventiva"),
    FALLA("Revisión por falla"),
    VENTA("Visita de venta"),
    TRANSPORTE("Entrega o retiro")
}

enum class EstadoEquipo(val etiqueta: String) {
    OPERATIVO("Operativo"),
    CON_OBSERVACIONES("Con observaciones"),
    CON_FALLA("Con falla")
}

enum class EstadoValidacion(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    VALIDADA("Validada"),
    RECHAZADA("Rechazada")
}

/** Convierte un texto guardado en Room al enum correspondiente, con valor por defecto seguro. */
inline fun <reified T : Enum<T>> String.aEnum(porDefecto: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: porDefecto
