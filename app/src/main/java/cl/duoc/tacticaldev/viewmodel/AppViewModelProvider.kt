package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModelProvider
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
        initializer { ActivosViewModel(app().container.activoRepository) }
    }
}

/** Obtiene la instancia de [TacticalApp] desde los CreationExtras. */
fun CreationExtras.app(): TacticalApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TacticalApp
