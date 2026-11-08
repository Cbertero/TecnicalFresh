package cl.duoc.tacticaldev.domain.validacion

import cl.duoc.tacticaldev.model.EstadoEquipo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Complementa ValidadoresTest con los casos borde: campos vacíos, textos no
 * numéricos y límites exactos. Un "caso borde" es el valor justo en el límite
 * de una regla (ej: 50 °C cuando el máximo es 50), donde suelen esconderse los errores.
 */
class ValidadoresCasosBordeTest : FunSpec({

    test("clave vacía pide ingresarla") {
        Validadores.clave("") shouldBe "Ingresa tu clave"
    }

    test("correo con espacios alrededor es válido") {
        Validadores.correo("  tecnico@demo.cl  ").shouldBeNull()
    }

    test("selección con valor es válida") {
        Validadores.seleccion(EstadoEquipo.OPERATIVO, "el estado").shouldBeNull()
    }

    test("temperatura vacía pide ingresarla") {
        Validadores.temperatura("  ") shouldBe "Ingresa la temperatura medida"
    }

    test("temperatura en los límites exactos es válida") {
        Validadores.temperatura("-35").shouldBeNull()
        Validadores.temperatura("50").shouldBeNull()
        Validadores.temperatura("50.1") shouldBe "Debe estar entre -35 y 50 °C"
    }

    test("sin estado seleccionado las observaciones no son obligatorias") {
        Validadores.observaciones("", null).shouldBeNull()
    }

    test("con observaciones el texto exige un mínimo") {
        Validadores.observaciones("corto", EstadoEquipo.CON_OBSERVACIONES) shouldBe
            "Describe el problema (mínimo 10 caracteres)"
    }

    test("observaciones de exactamente 500 caracteres son válidas") {
        Validadores.observaciones("a".repeat(500), EstadoEquipo.OPERATIVO).shouldBeNull()
    }

    test("motivo de rechazo vacío pide ingresarlo") {
        Validadores.motivoRechazo(" ") shouldBe "Ingresa el motivo del rechazo"
    }

    test("texto requerido muy largo o muy corto devuelve error") {
        Validadores.textoRequerido("a".repeat(61), "el nombre") shouldBe "Máximo 60 caracteres"
        Validadores.textoRequerido("ab", "el nombre") shouldBe "Mínimo 3 caracteres"
    }

    test("coordenadas vacías o no numéricas devuelven error") {
        Validadores.latitud("") shouldBe "Ingresa la latitud"
        Validadores.longitud("abc") shouldBe "Ingresa un número válido"
        Validadores.latitud("91") shouldBe "La latitud debe estar entre -90 y 90"
    }

    test("coordenadas con coma decimal son válidas") {
        Validadores.latitud("-33,45").shouldBeNull()
    }

    test("código de activo vacío pide ingresarlo") {
        Validadores.codigoActivo("") shouldBe "Ingresa el código del activo"
        Validadores.codigoActivo("TDV-CS-123").shouldBeNull()
    }
})
