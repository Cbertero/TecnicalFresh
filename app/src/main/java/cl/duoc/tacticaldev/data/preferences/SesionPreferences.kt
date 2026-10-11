package cl.duoc.tacticaldev.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.model.aEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

/** Datos mínimos del usuario conectado. */
data class Sesion(val usuarioId: Long, val nombre: String, val rol: Rol)

/**
 * Persiste la sesión con DataStore Preferences: si el operario cierra la app
 * en terreno, al volver sigue conectado.
 */
class SesionPreferences(private val context: Context) {

    val sesion: Flow<Sesion?> = context.dataStore.data.map { prefs ->
        val id = prefs[USUARIO_ID] ?: return@map null
        Sesion(
            usuarioId = id,
            nombre = prefs[NOMBRE].orEmpty(),
            rol = prefs[ROL].orEmpty().aEnum(Rol.OPERARIO)
        )
    }

    suspend fun guardar(sesion: Sesion) {
        context.dataStore.edit { prefs ->
            prefs[USUARIO_ID] = sesion.usuarioId
            prefs[NOMBRE] = sesion.nombre
            prefs[ROL] = sesion.rol.name
        }
    }

    suspend fun cerrar() {
        context.dataStore.edit { it.clear() }
    }

    private companion object {
        val USUARIO_ID = longPreferencesKey("usuario_id")
        val NOMBRE = stringPreferencesKey("nombre")
        val ROL = stringPreferencesKey("rol")
    }
}
