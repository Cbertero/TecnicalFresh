package cl.duoc.tacticaldev.ui.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/** Columnas de las grillas según el tamaño de pantalla (Window Size Classes). */
class TipoPantallaTest : FunSpec({

    test("un teléfono muestra una columna") {
        TipoPantalla.COMPACTA.columnas() shouldBe 1
    }

    test("una pantalla mediana (tablet vertical o plegable) muestra dos") {
        TipoPantalla.MEDIANA.columnas() shouldBe 2
    }

    test("una pantalla expandida (tablet horizontal) muestra tres") {
        TipoPantalla.EXPANDIDA.columnas() shouldBe 3
    }

    test("los tamaños van de menor a mayor") {
        TipoPantalla.entries.toList() shouldContainExactly
            listOf(TipoPantalla.COMPACTA, TipoPantalla.MEDIANA, TipoPantalla.EXPANDIDA)
        TipoPantalla.valueOf("MEDIANA") shouldBe TipoPantalla.MEDIANA
    }
})
