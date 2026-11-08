package cl.duoc.tacticaldev.domain.validacion

import cl.duoc.tacticaldev.model.EstadoEquipo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Pruebas unitarias de las reglas de validación centralizadas.
 * Se ejecutan en la JVM (sin emulador): ./gradlew testDebugUnitTest
 *
 * `context("...")` agrupa pruebas relacionadas; en el reporte aparecen anidadas.
 * Cada validador devuelve el mensaje de error, o null si el valor es válido.
 */
class ValidadoresTest : FunSpec({

    context("Login") {
        test("correo vacío devuelve error") {
            Validadores.correo("") shouldBe "Ingresa tu correo"
        }
        test("correo sin arroba devuelve error") {
            Validadores.correo("tecnico.demo.cl").shouldNotBeNull()
        }
        test("correo válido no devuelve error") {
            Validadores.correo("tecnico@demo.cl").shouldBeNull()
        }
        test("clave corta devuelve error") {
            Validadores.clave("12345").shouldNotBeNull()
        }
        test("clave con el largo mínimo es válida") {
            Validadores.clave("123456").shouldBeNull()
        }
    }

    context("Formulario de visita") {
        test("selección nula devuelve error") {
            Validadores.seleccion(null, "el tipo de visita") shouldBe "Selecciona el tipo de visita"
        }
        test("temperatura no numérica devuelve error") {
            Validadores.temperatura("frío") shouldBe "Ingresa un número válido"
        }
        test("temperatura fuera de rango devuelve error") {
            // El rango válido es de -35 a 50 °C
            Validadores.temperatura("51").shouldNotBeNull()
            Validadores.temperatura("-40").shouldNotBeNull()
        }
        test("temperatura con coma decimal es válida") {
            Validadores.temperatura("-18,5").shouldBeNull()
        }
        test("temperatura dentro del rango es válida") {
            Validadores.temperatura("-30").shouldBeNull()
            Validadores.temperatura("15").shouldBeNull()
        }
        test("con el equipo operativo las observaciones pueden ir vacías") {
            Validadores.observaciones("", EstadoEquipo.OPERATIVO).shouldBeNull()
        }
        test("con el equipo con falla las observaciones son obligatorias") {
            Validadores.observaciones("No enfría", EstadoEquipo.CON_FALLA).shouldNotBeNull()
            Validadores.observaciones("No enfría, compresor detenido", EstadoEquipo.CON_FALLA).shouldBeNull()
        }
        test("observaciones muy largas devuelven error") {
            Validadores.observaciones("a".repeat(501), EstadoEquipo.OPERATIVO).shouldNotBeNull()
        }
        test("sin foto devuelve error") {
            Validadores.foto(null).shouldNotBeNull()
            Validadores.foto("").shouldNotBeNull()
            Validadores.foto("content://cl.duoc.tacticaldev.fileprovider/evidencias/visita.jpg").shouldBeNull()
        }
        test("ubicación incompleta devuelve error") {
            Validadores.ubicacion(null, null).shouldNotBeNull()
            Validadores.ubicacion(-33.45, null).shouldNotBeNull()
            Validadores.ubicacion(-33.45, -70.66).shouldBeNull()
        }
        test("confirmación no aceptada devuelve error") {
            Validadores.confirmacion(false).shouldNotBeNull()
            Validadores.confirmacion(true).shouldBeNull()
        }
    }

    context("Supervisor") {
        test("motivo de rechazo corto devuelve error") {
            Validadores.motivoRechazo("Foto mala").shouldNotBeNull()
            Validadores.motivoRechazo("La foto no muestra el equipo").shouldBeNull()
        }
    }

    context("Administración") {
        test("código de activo con formato incorrecto devuelve error") {
            Validadores.codigoActivo("VF-001").shouldNotBeNull()
            Validadores.codigoActivo("TDV-XX-001").shouldNotBeNull()
        }
        test("código de activo en minúsculas se acepta normalizado") {
            Validadores.codigoActivo("tdv-vf-004").shouldBeNull()
        }
        test("coordenadas fuera de rango devuelven error") {
            Validadores.latitud("-95").shouldNotBeNull()
            Validadores.longitud("200").shouldNotBeNull()
            Validadores.latitud("-33.4489").shouldBeNull()
            Validadores.longitud("-70.6693").shouldBeNull()
        }
        test("texto requerido respeta sus límites") {
            Validadores.textoRequerido("   ", "el nombre") shouldBe "Ingresa el nombre"
            Validadores.textoRequerido("ab", "el nombre").shouldNotBeNull()
            Validadores.textoRequerido("Minimarket Sol", "el nombre").shouldBeNull()
        }
    }
})
