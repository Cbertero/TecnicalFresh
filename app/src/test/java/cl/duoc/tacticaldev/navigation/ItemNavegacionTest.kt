package cl.duoc.tacticaldev.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import cl.duoc.tacticaldev.model.Rol
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ItemNavegacionTest : FunSpec({

    test("un ítem guarda su ruta, etiqueta e ícono") {
        val item = ItemNavegacion(Screen.Dashboard.route, "Inicio", Icons.Filled.Home)

        item.ruta shouldBe "dashboard"
        item.etiqueta shouldBe "Inicio"
        item.icono shouldBe Icons.Filled.Home
    }

    test("dos ítems iguales se comparan por valor") {
        val item = ItemNavegacion("mis_visitas", "Mis visitas", Icons.Filled.History)

        item shouldBe item.copy()
        item shouldNotBe item.copy(etiqueta = "Otra")
    }
})

/** Pantalla de inicio de cada rol después del login. */
class RutaInicioTest : FunSpec({

    test("el operario entra a su panel") {
        Rol.OPERARIO.rutaInicio() shouldBe Screen.Dashboard.route
    }

    test("el supervisor entra al panel de supervisión") {
        Rol.SUPERVISOR.rutaInicio() shouldBe Screen.DashboardSupervisor.route
    }
})
