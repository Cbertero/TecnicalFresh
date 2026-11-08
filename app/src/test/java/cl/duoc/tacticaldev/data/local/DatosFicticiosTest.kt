package cl.duoc.tacticaldev.data.local

import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.utils.Seguridad
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContainDuplicates
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/** Verifica que los datos semilla sean coherentes entre sí (claves foráneas, hash, etc.). */
class DatosFicticiosTest : FunSpec({

    test("hay cuatro usuarios y todos usan la clave demo") {
        val usuarios = DatosFicticios.usuarios()
        usuarios shouldHaveSize 4
        val hashEsperado = Seguridad.sha256(DatosFicticios.CLAVE_DEMO)
        usuarios.all { it.claveHash == hashEsperado } shouldBe true
    }

    test("hay un solo supervisor") {
        DatosFicticios.usuarios().count { it.rol == Rol.SUPERVISOR.name } shouldBe 1
    }

    test("todos los activos apuntan a puntos de venta existentes") {
        val idsPuntos = DatosFicticios.puntosVenta().map { it.id }.toSet()
        idsPuntos shouldHaveSize 4
        DatosFicticios.activos().all { it.puntoVentaId in idsPuntos } shouldBe true
    }

    test("los códigos de los activos no se repiten") {
        DatosFicticios.activos().map { it.codigo }.shouldNotContainDuplicates()
    }

    test("las visitas usan la hora recibida y referencias válidas") {
        val ahora = 1_000_000_000L
        val visitas = DatosFicticios.visitas(ahora)
        val idsActivos = DatosFicticios.activos().map { it.id }.toSet()
        val idsUsuarios = DatosFicticios.usuarios().map { it.id }.toSet()

        visitas shouldHaveSize 3
        visitas.all { it.fechaHora < ahora } shouldBe true
        visitas.all { it.activoId in idsActivos && it.usuarioId in idsUsuarios } shouldBe true
        visitas.first().estadoValidacion shouldBe EstadoValidacion.VALIDADA.name
    }

    test("sin parámetro, las visitas usan la hora actual") {
        val antes = System.currentTimeMillis()
        DatosFicticios.visitas().all { it.fechaHora < antes + 1 } shouldBe true
    }
})

class TecnicoModelTest : FunSpec({

    test("un Tecnico nuevo tiene valores por defecto correctos") {
        val tecnico = Tecnico(nombre = "Ana", cargo = "Vendedora")
        tecnico.id.shouldBeNull()
        tecnico.localAsignado shouldBe ""
        tecnico.sincronizado shouldBe true
    }
})

class RetrofitInstanceTest : FunSpec({

    test("el cliente de la API se crea correctamente") {
        // Solo construye el cliente; no hace ninguna llamada a internet.
        RetrofitInstance.api.shouldNotBeNull()
    }
})
