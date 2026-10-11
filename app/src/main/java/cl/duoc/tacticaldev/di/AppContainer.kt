package cl.duoc.tacticaldev.di

import android.content.Context
import cl.duoc.tacticaldev.data.local.AppDatabase
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.UsuarioRepository
import cl.duoc.tacticaldev.data.repository.VisitaRepository

/**
 * Inyección de dependencias manual: un único lugar donde se crean la base de
 * datos y los repositorios que consumen los ViewModels.
 */
class AppContainer(context: Context) {
    val database: AppDatabase by lazy { AppDatabase.obtener(context) }

    val usuarioRepository by lazy { UsuarioRepository(database.usuarioDao()) }
    val activoRepository by lazy { ActivoRepository(database.activoDao(), database.puntoVentaDao()) }
    val visitaRepository by lazy { VisitaRepository(database.visitaDao()) }

    val sesionPreferences by lazy { SesionPreferences(context.applicationContext) }
}
