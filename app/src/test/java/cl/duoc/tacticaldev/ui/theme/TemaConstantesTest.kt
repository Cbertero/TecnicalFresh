package cl.duoc.tacticaldev.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Valores fijos del tema (espaciado, colores y tipografía). Son constantes de Kotlin,
 * así que se prueban directo en la JVM sin dibujar nada.
 */
class TemaConstantesTest : FunSpec({

    test("la escala de espaciado crece de 4 en 4 dp") {
        Espaciado.xs shouldBe 4.dp
        Espaciado.s shouldBe 8.dp
        Espaciado.m shouldBe 12.dp
        Espaciado.l shouldBe 16.dp
        Espaciado.xl shouldBe 24.dp
        Espaciado.anchoMaximoFormulario shouldBe 640.dp
    }

    test("la paleta oscura usa los tonos Arctic Cyan") {
        FondoNocheGlacial shouldBe Color(0xFF131D2E)
        SuperficieTarjeta shouldBe Color(0xFF1D2B42)
        BordeTarjetaSutil shouldBe Color(0xFF2E4366)
        BordeCianGlacial shouldBe Color(0xFF22D3EE)
        CianFrio shouldBe Color(0xFF38BDF8)
        CianFrioOscuro shouldBe Color(0xFF0369A1)
        CianContenedor shouldBe Color(0xFF0C4A6E)
        TextoBlancoPuro shouldBe Color(0xFFF8FAFC)
        TextoSecundarioGris shouldBe Color(0xFF94A3B8)
    }

    test("la paleta clara usa fondos con tinte frío") {
        FondoHieloClaro shouldBe Color(0xFFF1F7FB)
        SuperficieClara shouldBe Color.White
        SuperficieVarianteClara shouldBe Color(0xFFE2EEF6)
        CianContenedorClaro shouldBe Color(0xFFE0F2FE)
        CianSecundarioClaro shouldBe Color(0xFF0E7490)
        TextoPizarraOscuro shouldBe Color(0xFF0F172A)
        TextoSecundarioPizarra shouldBe Color(0xFF475569)
    }

    test("cada color semántico tiene una variante distinta para cada modo") {
        VerdeExitoOscuro shouldNotBe VerdeExitoClaro
        AmbarAlertaOscuro shouldNotBe AmbarAlertaClaro
        RojoErrorOscuro shouldNotBe RojoErrorClaro
        VerdeExitoContenedorOscuro shouldNotBe VerdeExitoContenedorClaro
        AmbarAlertaContenedorOscuro shouldNotBe AmbarAlertaContenedorClaro
        RojoErrorContenedorOscuro shouldNotBe RojoErrorContenedorClaro
    }

    test("los títulos van en negrita y el cuerpo en peso normal") {
        Typography.headlineMedium.fontWeight shouldBe FontWeight.Bold
        Typography.headlineMedium.fontSize shouldBe 26.sp
        Typography.titleLarge.fontWeight shouldBe FontWeight.SemiBold
        Typography.titleLarge.fontSize shouldBe 20.sp
        Typography.titleMedium.fontSize shouldBe 16.sp
        Typography.bodyLarge.fontWeight shouldBe FontWeight.Normal
        Typography.bodyLarge.lineHeight shouldBe 24.sp
        Typography.bodyMedium.fontSize shouldBe 14.sp
        Typography.labelLarge.fontWeight shouldBe FontWeight.Medium
    }
})
