package cl.duoc.tacticaldev.data.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** Cuerpo que se envía al microservicio al validar o rechazar una visita. */
class ValidacionRequestTest : FunSpec({

    test("al validar solo se envía el supervisor (sin motivo)") {
        val solicitud = ValidacionRequest(supervisorId = 4)

        solicitud.supervisorId shouldBe 4
        solicitud.motivo.shouldBeNull()
    }

    test("al rechazar se envía también el motivo") {
        val solicitud = ValidacionRequest(supervisorId = 4, motivo = "Foto borrosa")

        solicitud.motivo shouldBe "Foto borrosa"
    }

    test("dos solicitudes con los mismos datos son iguales") {
        ValidacionRequest(4, "x") shouldBe ValidacionRequest(4, "x")
        ValidacionRequest(4, "x") shouldNotBe ValidacionRequest(4, "y")
        ValidacionRequest(4).copy(motivo = "z") shouldBe ValidacionRequest(4, "z")
    }
})
