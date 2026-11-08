package cl.duoc.tacticaldev.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import java.util.TimeZone

/**
 * Pruebas de funciones puras: reciben un valor y devuelven otro, sin depender de Android.
 *
 * Cómo se lee Kotest (estilo FunSpec):
 *   class MiPrueba : FunSpec({
 *       test("descripción en lenguaje natural") {
 *           valorObtenido shouldBe valorEsperado
 *       }
 *   })
 * Cada prueba sigue el patrón AAA: Arrange (preparar), Act (ejecutar), Assert (comprobar).
 */
class GeoUtilsTest : FunSpec({

    test("la distancia entre un punto y sí mismo es cero") {
        GeoUtils.distanciaMetros(-33.45, -70.66, -33.45, -70.66) shouldBe (0.0 plusOrMinus 0.001)
    }

    test("un grado de latitud mide aproximadamente 111 km") {
        // 1 grado = radio de la Tierra * PI / 180 ≈ 111.195 m
        // plusOrMinus indica el margen de error aceptado (los decimales nunca son exactos)
        GeoUtils.distanciaMetros(0.0, 0.0, 1.0, 0.0) shouldBe (111_195.0 plusOrMinus 1.0)
    }

    test("la distancia es igual de ida que de vuelta") {
        val ida = GeoUtils.distanciaMetros(-33.5160, -70.7170, -33.4489, -70.6693)
        val vuelta = GeoUtils.distanciaMetros(-33.4489, -70.6693, -33.5160, -70.7170)
        ida shouldBe (vuelta plusOrMinus 0.0001)
    }

    test("estar dentro del radio respeta el límite de 150 metros") {
        GeoUtils.estaDentroDelRadio(0.0) shouldBe true
        GeoUtils.estaDentroDelRadio(GeoUtils.RADIO_VERIFICACION_METROS) shouldBe true
        GeoUtils.estaDentroDelRadio(150.1) shouldBe false
    }
})

class FormatosTest : FunSpec({

    // beforeTest/afterTest se ejecutan antes y después de CADA prueba.
    // Fijamos la zona horaria en UTC para que el resultado no dependa del computador.
    val zonaOriginal = TimeZone.getDefault()
    beforeTest { TimeZone.setDefault(TimeZone.getTimeZone("UTC")) }
    afterTest { TimeZone.setDefault(zonaOriginal) }

    test("fechaHora usa el formato día-mes-año hora:minuto") {
        Formatos.fechaHora(0L) shouldBe "01-01-1970 00:00"
    }

    test("las distancias menores a mil se muestran en metros") {
        Formatos.distancia(999.9) shouldBe "999 m"
        Formatos.distancia(14.0) shouldBe "14 m"
    }

    test("desde mil metros se muestran en kilómetros con coma decimal") {
        Formatos.distancia(1500.0) shouldBe "1,5 km"
        Formatos.distancia(1000.0) shouldBe "1,0 km"
    }
})

class SeguridadTest : FunSpec({

    test("el SHA-256 de un texto conocido coincide con el valor estándar") {
        Seguridad.sha256("abc") shouldBe "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    }

    test("el hash siempre tiene 64 caracteres hexadecimales") {
        val hash = Seguridad.sha256("Clave123")
        hash shouldHaveLength 64
        hash shouldMatch Regex("[0-9a-f]+")
    }

    test("textos distintos producen hashes distintos") {
        Seguridad.sha256("Clave123") shouldNotBe Seguridad.sha256("clave123")
    }

    test("el mismo texto siempre produce el mismo hash") {
        Seguridad.sha256("Clave123") shouldBe Seguridad.sha256("Clave123")
    }
})
