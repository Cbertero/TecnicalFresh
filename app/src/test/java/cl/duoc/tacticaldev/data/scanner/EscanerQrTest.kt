package cl.duoc.tacticaldev.data.scanner

import android.content.Context
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest

/**
 * Escáner de QR de Google Play services. En la JVM no existe la cámara, así que
 * simulamos (MockK) el cliente del escáner y su tarea (Task): cada prueba decide
 * qué "contesta" Google: un código leído, una cancelación o un error.
 */
class EscanerQrTest : FunSpec({

    val context: Context = mockk(relaxed = true)
    val escaner: GmsBarcodeScanner = mockk()
    val tarea: Task<Barcode> = mockk()

    beforeTest {
        mockkStatic(GmsBarcodeScanning::class)
        every { GmsBarcodeScanning.getClient(any<Context>(), any<GmsBarcodeScannerOptions>()) } returns escaner
        every { escaner.startScan() } returns tarea
        // Por defecto los listeners solo se registran (no responden)
        every { tarea.addOnSuccessListener(any<OnSuccessListener<Barcode>>()) } returns tarea
        every { tarea.addOnCanceledListener(any<OnCanceledListener>()) } returns tarea
        every { tarea.addOnFailureListener(any<OnFailureListener>()) } returns tarea
    }

    afterTest { unmockkStatic(GmsBarcodeScanning::class) }

    /** Simula que Google leyó un QR con el texto indicado. */
    fun responderCodigo(texto: String?) {
        val codigo: Barcode = mockk { every { rawValue } returns texto }
        every { tarea.addOnSuccessListener(any<OnSuccessListener<Barcode>>()) } answers {
            firstArg<OnSuccessListener<Barcode>>().onSuccess(codigo)
            tarea
        }
    }

    test("devuelve Exito con el texto del QR leído") {
        runTest {
            responderCodigo("TDV-VF-001")

            EscanerQr.escanear(context) shouldBe ResultadoEscaneo.Exito("TDV-VF-001")
            verify { escaner.startScan() }
        }
    }

    test("un QR sin texto se informa como error") {
        runTest {
            responderCodigo("   ")

            EscanerQr.escanear(context) shouldBe ResultadoEscaneo.Error("El código QR está vacío")
        }
    }

    test("un QR con texto nulo también es error") {
        runTest {
            responderCodigo(null)

            EscanerQr.escanear(context) shouldBe ResultadoEscaneo.Error("El código QR está vacío")
        }
    }

    test("si el usuario cierra el escáner devuelve Cancelado") {
        runTest {
            every { tarea.addOnCanceledListener(any<OnCanceledListener>()) } answers {
                firstArg<OnCanceledListener>().onCanceled()
                tarea
            }

            EscanerQr.escanear(context) shouldBe ResultadoEscaneo.Cancelado
        }
    }

    test("si Google falla devuelve el mensaje del error") {
        runTest {
            every { tarea.addOnFailureListener(any<OnFailureListener>()) } answers {
                firstArg<OnFailureListener>().onFailure(Exception("Cámara ocupada"))
                tarea
            }

            EscanerQr.escanear(context) shouldBe ResultadoEscaneo.Error("Cámara ocupada")
        }
    }

    test("si el error no trae mensaje usa uno por defecto") {
        runTest {
            every { tarea.addOnFailureListener(any<OnFailureListener>()) } answers {
                firstArg<OnFailureListener>().onFailure(Exception())
                tarea
            }

            EscanerQr.escanear(context) shouldBe ResultadoEscaneo.Error("No se pudo abrir el escáner")
        }
    }

    test("ResultadoEscaneo compara por valor") {
        ResultadoEscaneo.Exito("A") shouldBe ResultadoEscaneo.Exito("A")
        ResultadoEscaneo.Exito("A") shouldNotBe ResultadoEscaneo.Exito("B")
        ResultadoEscaneo.Error("x").mensaje shouldBe "x"
        ResultadoEscaneo.Exito("QR").codigo shouldBe "QR"
        ResultadoEscaneo.Cancelado.toString() shouldBe "Cancelado"
    }
})
