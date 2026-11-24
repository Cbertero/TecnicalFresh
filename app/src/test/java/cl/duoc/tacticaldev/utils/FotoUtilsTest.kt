package cl.duoc.tacticaldev.utils

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.Deflater

/** Manejo de las fotos de evidencia (FileProvider, borrado y conversión a Base64). */
@RunWith(AndroidJUnit4::class)
class FotoUtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /**
     * Imagen PNG real de un solo color para simular la foto que entrega la cámara.
     * Se arma "a mano" con java.util.zip porque en Android no existe java.awt.
     */
    private fun imagenPng(ancho: Int, alto: Int): ByteArray {
        // Cada fila: 1 byte de filtro (0) + 3 bytes RGB por píxel
        val fila = ByteArray(1 + ancho * 3) { i -> (if (i == 0) 0 else 0x60).toByte() }
        val pixeles = ByteArrayOutputStream().apply { repeat(alto) { write(fila) } }.toByteArray()
        val comprimidos = ByteArrayOutputStream().also { salida ->
            val deflater = Deflater()
            deflater.setInput(pixeles)
            deflater.finish()
            val buffer = ByteArray(8_192)
            while (!deflater.finished()) salida.write(buffer, 0, deflater.deflate(buffer))
            deflater.end()
        }.toByteArray()

        val png = ByteArrayOutputStream()
        val datos = DataOutputStream(png)
        fun bloque(tipo: String, contenido: ByteArray) {
            val bytesTipo = tipo.toByteArray(Charsets.US_ASCII)
            datos.writeInt(contenido.size)
            datos.write(bytesTipo)
            datos.write(contenido)
            datos.writeInt(CRC32().apply { update(bytesTipo); update(contenido) }.value.toInt())
        }
        datos.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) // firma PNG
        val cabecera = ByteArrayOutputStream().also { c ->
            DataOutputStream(c).apply {
                writeInt(ancho); writeInt(alto)
                write(byteArrayOf(8, 2, 0, 0, 0)) // 8 bits, RGB, sin entrelazado
            }
        }.toByteArray()
        bloque("IHDR", cabecera)
        bloque("IDAT", comprimidos)
        bloque("IEND", ByteArray(0))
        return png.toByteArray()
    }

    @Test
    fun eliminar_noFallaAunqueElArchivoNoExista() {
        // runCatching dentro de eliminar evita que un URI inválido cierre la app
        FotoUtils.eliminar(context, Uri.parse("content://${context.packageName}.fileprovider/evidencias/no_existe.jpg"))
    }

    @Test
    fun aBase64_textoVacio_devuelveNull() {
        FotoUtils.aBase64(context, "").shouldBeNull()
        FotoUtils.aBase64(context, "   ").shouldBeNull()
    }

    @Test
    fun aBase64_fotoQueYaVieneDelServidor_seDevuelveIgual() {
        val base64 = "data:image/jpeg;base64,AAAA"

        FotoUtils.aBase64(context, base64) shouldBe base64
    }

    @Test
    fun aBase64_fotoGrande_seComprimeComoJpegEnBase64() {
        val uri = Uri.parse("content://cl.duoc.tacticaldev.fileprovider/evidencias/grande.jpg")
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(imagenPng(1600, 1200)))

        FotoUtils.aBase64(context, uri.toString()).shouldNotBeNull() shouldStartWith "data:image/jpeg;base64,"
    }

    @Test
    fun aBase64_fotoPequena_tambienSeConvierte() {
        val uri = Uri.parse("content://cl.duoc.tacticaldev.fileprovider/evidencias/chica.jpg")
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(imagenPng(200, 100)))

        FotoUtils.aBase64(context, uri.toString()).shouldNotBeNull() shouldStartWith "data:image/jpeg;base64,"
    }

    @Test
    fun aBase64_siNoSePuedeLeerLaFoto_devuelveNull() {
        // Foto que ya no existe (por ejemplo, el usuario la borró de la galería)
        FotoUtils.aBase64(context, "content://cl.duoc.tacticaldev.fileprovider/evidencias/no_existe.jpg").shouldBeNull()
    }
}
