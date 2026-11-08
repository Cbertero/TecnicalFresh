package cl.duoc.tacticaldev.navigation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ScreenTest : FunSpec({

    test("las rutas simples tienen el nombre esperado") {
        Screen.Login.route shouldBe "login"
        Screen.Dashboard.route shouldBe "dashboard"
        Screen.InicioOperario.route shouldBe "inicio_operario"
        Screen.MisVisitas.route shouldBe "mis_visitas"
        Screen.DashboardSupervisor.route shouldBe "dashboard_supervisor"
        Screen.Supervisor.route shouldBe "supervisor"
        Screen.Administracion.route shouldBe "administracion"
        Screen.Equipo.route shouldBe "equipo"
        Screen.Tecnicos.route shouldBe "tecnicos"
    }

    test("las rutas con argumento declaran el parámetro") {
        Screen.FormularioVisita.route shouldBe "formulario_visita/{activoId}"
        Screen.Confirmacion.route shouldBe "confirmacion/{visitaId}"
        Screen.HistorialActivo.route shouldBe "historial_activo/{activoId}"
    }

    test("crear reemplaza el argumento por el id") {
        Screen.FormularioVisita.crear(5) shouldBe "formulario_visita/5"
        Screen.Confirmacion.crear(12) shouldBe "confirmacion/12"
        Screen.HistorialActivo.crear(3) shouldBe "historial_activo/3"
    }

    test("NavigateTo tiene valores por defecto seguros") {
        val evento = NavigationEvent.NavigateTo("login")
        evento.popUpToRoute.shouldBeNull()
        evento.inclusive shouldBe false
        evento.limpiarPila shouldBe false
    }
})
