 package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tacticaldev.TacticalApp

/**
 * Fábrica única de ViewModels: entrega a cada ViewModel los repositorios
 * del AppContainer (las vistas nunca acceden directo a los datos).
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { SesionViewModel(app().container.sesionPreferences) }
        initializer {
            LoginViewModel(app().container.usuarioRepository, app().container.sesionPreferences)
        }
        initializer {
            RegistroViewModel(app().container.usuarioRepository)
        }
        initializer { ActivosViewModel(app().container.activoRepository) }
        initializer {
            VisitaViewModel(
                savedStateHandle = createSavedStateHandle(),
                activoRepository = app().container.activoRepository,
                visitaRepository = app().container.visitaRepository,
                sesionPreferences = app().container.sesionPreferences,
                ubicacionService = app().container.ubicacionService
            )
        }
        initializer {
            ConfirmacionViewModel(createSavedStateHandle(), app().container.visitaRepository)
        }
        initializer {
            HistorialViewModel(
                createSavedStateHandle(),
                app().container.activoRepository,
                app().container.visitaRepository
            )
        }
        initializer {
            MisVisitasViewModel(app().container.sesionPreferences, app().container.visitaRepository)
        }
        initializer {
            SupervisorViewModel(app().container.visitaRepository, app().container.sesionPreferences)
        }
        initializer {
            AdminViewModel(app().container.activoRepository, app().container.ubicacionService)
        }
    }
}

/** Obtiene la instancia de [TacticalApp] desde los CreationExtras. */
fun CreationExtras.app(): TacticalApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TacticalApp
