package cl.duoc.tacticaldev.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/** Ícono de cada tipo de alerta flotante (el color se prueba en AlertaYTemaComposeTest). */
class TipoAlertaTest : FunSpec({

    test("cada tipo de alerta tiene su ícono") {
        TipoAlerta.EXITO.icono shouldBe Icons.Default.CheckCircle
        TipoAlerta.ERROR.icono shouldBe Icons.Default.Cancel
        TipoAlerta.INFO.icono shouldBe Icons.Default.Info
    }

    test("existen tres tipos de alerta") {
        TipoAlerta.entries.toList() shouldContainExactly listOf(TipoAlerta.EXITO, TipoAlerta.ERROR, TipoAlerta.INFO)
    }
})
