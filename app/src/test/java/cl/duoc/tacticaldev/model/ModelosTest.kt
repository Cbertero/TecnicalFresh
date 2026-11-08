package cl.duoc.tacticaldev.model

import cl.duoc.tacticaldev.testutil.DatosPrueba
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/** Pruebas de los enums, la conversión String -> enum y los estados de UI. */
class EnumsTest : FunSpec({

    test("aEnum con un texto válido devuelve el enum correcto") {
        "SUPERVISOR".aEnum(Rol.OPERARIO) shouldBe Rol.SUPERVISOR
        "FALLA".aEnum(TipoVisita.MANTENCION) shouldBe TipoVisita.FALLA
    }

    test("aEnum con un texto inválido devuelve el valor por defecto") {
        "ADMIN".aEnum(Rol.OPERARIO) shouldBe Rol.OPERARIO
        "".aEnum(EstadoEquipo.OPERATIVO) shouldBe EstadoEquipo.OPERATIVO
    }

    test("las etiquetas son los textos que ve el usuario") {
        Rol.SUPERVISOR.etiqueta shouldBe "Supervisor"
        TipoActivo.VITRINA_FREEZER.etiqueta shouldBe "Vitrina freezer"
        TipoVisita.FALLA.etiqueta shouldBe "Revisión por falla"
        EstadoEquipo.CON_FALLA.etiqueta shouldBe "Con falla"
        EstadoValidacion.RECHAZADA.etiqueta shouldBe "Rechazada"
    }

    test("los catálogos tienen la cantidad esperada de opciones") {
        Rol.entries shouldHaveSize 2
        TipoActivo.entries shouldHaveSize 2
        TipoVisita.entries shouldHaveSize 4
        EstadoEquipo.entries shouldHaveSize 3
        EstadoValidacion.entries shouldHaveSize 3
    }
})

class EstadosUiTest : FunSpec({

    test("VisitaErrores sin errores informa que no hay errores") {
        VisitaErrores().hayErrores shouldBe false
    }

    test("VisitaErrores con un solo error informa que hay errores") {
        VisitaErrores(foto = "Falta la foto").hayErrores shouldBe true
        VisitaErrores(confirmacion = "Confirma").hayErrores shouldBe true
    }

    test("PuntoVentaErrores detecta un error en cualquier campo") {
        PuntoVentaErrores().hayErrores shouldBe false
        PuntoVentaErrores(longitud = "error").hayErrores shouldBe true
    }

    test("ActivoErrores detecta un error en cualquier campo") {
        ActivoErrores().hayErrores shouldBe false
        ActivoErrores(puntoVenta = "error").hayErrores shouldBe true
    }

    test("los estados iniciales tienen valores por defecto correctos") {
        val visita = VisitaFormUiState()
        visita.cargando shouldBe true
        visita.tipoVisita.shouldBeNull()
        visita.temperatura shouldBe ""

        val login = LoginUiState()
        login.intentosFallidos shouldBe 0
        login.errores.correo.shouldBeNull()

        val supervisor = SupervisorUiState()
        supervisor.filtro shouldBe EstadoValidacion.PENDIENTE
        supervisor.visitas.shouldBeEmpty()

        val rechazo = RechazoUiState()
        rechazo.visita.shouldBeNull()
        rechazo.motivo shouldBe ""

        val admin = AdminUiState()
        admin.pestana shouldBe 0
        admin.activo.tipo.shouldBeNull()
        admin.puntoVenta.nombre shouldBe ""
    }

    test("tipoActivo convierte el texto guardado en Room") {
        val activo = DatosPrueba.activoConPunto().copy(tipo = "CONSERVADORA")
        activo.tipoActivo shouldBe TipoActivo.CONSERVADORA
        activo.copy(tipo = "OTRO").tipoActivo shouldBe TipoActivo.VITRINA_FREEZER
    }

    test("las propiedades de extensión de VisitaDetalle convierten los textos") {
        val visita = DatosPrueba.visitaDetalle().copy(
            tipoVisita = "VENTA",
            estadoEquipo = "CON_FALLA",
            estadoValidacion = "VALIDADA"
        )
        visita.tipo shouldBe TipoVisita.VENTA
        visita.estado shouldBe EstadoEquipo.CON_FALLA
        visita.validacion shouldBe EstadoValidacion.VALIDADA
    }
})
