package cl.duoc.tacticaldev.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest

/**
 * Servicio de GPS. Se simulan el cliente "Fused Location" de Google y la
 * verificación de permisos, así se prueban todos los caminos sin un teléfono:
 * sin permiso, ubicación obtenida, error del GPS y GPS que nunca responde.
 */
class UbicacionServiceTest : FunSpec({

    val contexto: Context = mockk(relaxed = true)
    val cliente: FusedLocationProviderClient = mockk()
    val tarea: Task<Location> = mockk()
    val ubicacion: Location = mockk()

    beforeTest {
        mockkStatic(LocationServices::class, ContextCompat::class)
        every { LocationServices.getFusedLocationProviderClient(any<Context>()) } returns cliente
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { cliente.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns tarea
        every { tarea.addOnSuccessListener(any<OnSuccessListener<Location>>()) } returns tarea
        every { tarea.addOnFailureListener(any<OnFailureListener>()) } returns tarea
    }

    afterTest { unmockkStatic(LocationServices::class, ContextCompat::class) }

    fun concederPermiso(permiso: String) {
        every { ContextCompat.checkSelfPermission(any(), permiso) } returns PackageManager.PERMISSION_GRANTED
    }

    context("tienePermiso") {
        test("es falso si no hay ningún permiso de ubicación") {
            UbicacionService(contexto).tienePermiso() shouldBe false
        }

        test("basta con la ubicación precisa") {
            concederPermiso(Manifest.permission.ACCESS_FINE_LOCATION)
            UbicacionService(contexto).tienePermiso() shouldBe true
        }

        test("basta con la ubicación aproximada") {
            concederPermiso(Manifest.permission.ACCESS_COARSE_LOCATION)
            UbicacionService(contexto).tienePermiso() shouldBe true
        }
    }

    context("obtenerUbicacionActual") {
        test("sin permiso devuelve null y no consulta el GPS") {
            runTest {
                UbicacionService(contexto).obtenerUbicacionActual().shouldBeNull()
                verify(exactly = 0) { cliente.getCurrentLocation(any<Int>(), any<CancellationToken>()) }
            }
        }

        test("con permiso devuelve la ubicación entregada por el GPS") {
            runTest {
                concederPermiso(Manifest.permission.ACCESS_FINE_LOCATION)
                every { tarea.addOnSuccessListener(any<OnSuccessListener<Location>>()) } answers {
                    firstArg<OnSuccessListener<Location>>().onSuccess(ubicacion)
                    tarea
                }

                UbicacionService(contexto).obtenerUbicacionActual() shouldBeSameInstanceAs ubicacion
            }
        }

        test("si el GPS falla devuelve null") {
            runTest {
                concederPermiso(Manifest.permission.ACCESS_FINE_LOCATION)
                every { tarea.addOnFailureListener(any<OnFailureListener>()) } answers {
                    firstArg<OnFailureListener>().onFailure(Exception("GPS apagado"))
                    tarea
                }

                UbicacionService(contexto).obtenerUbicacionActual().shouldBeNull()
            }
        }

        test("si el GPS nunca responde, a los 15 segundos devuelve null") {
            // runTest usa un reloj virtual: los 15 s de espera pasan al instante
            runTest {
                concederPermiso(Manifest.permission.ACCESS_COARSE_LOCATION)

                UbicacionService(contexto).obtenerUbicacionActual().shouldBeNull()
                verify { cliente.getCurrentLocation(any<Int>(), any<CancellationToken>()) }
            }
        }
    }
})
