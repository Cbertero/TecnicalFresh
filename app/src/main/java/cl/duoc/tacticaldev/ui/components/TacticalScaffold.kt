package cl.duoc.tacticaldev.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import cl.duoc.tacticaldev.navigation.ItemNavegacion
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.TipoPantalla

/**
 * Estructura visual común de las pantallas: TopAppBar + navegación.
 * En pantallas compactas usa NavigationBar inferior y en medianas/expandidas
 * un NavigationRail lateral (adaptabilidad, Guía 9 y 10).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalScaffold(
    titulo: String,
    modifier: Modifier = Modifier,
    itemsNavegacion: List<ItemNavegacion> = emptyList(),
    rutaActual: String? = null,
    onNavegar: (String) -> Unit = {},
    onVolver: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    floatingActionButton: @Composable () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    val usarRiel = itemsNavegacion.isNotEmpty() && LocalTipoPantalla.current != TipoPantalla.COMPACTA

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (onCerrarSesion != null) {
                        IconButton(onClick = onCerrarSesion) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (itemsNavegacion.isNotEmpty() && !usarRiel) {
                NavigationBar {
                    itemsNavegacion.forEach { item ->
                        NavigationBarItem(
                            selected = item.ruta == rutaActual,
                            onClick = { onNavegar(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = null) },
                            label = { Text(item.etiqueta) }
                        )
                    }
                }
            }
        },
        snackbarHost = { if (snackbarHostState != null) SnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton
    ) { padding ->
        if (usarRiel) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                NavigationRail {
                    itemsNavegacion.forEach { item ->
                        NavigationRailItem(
                            selected = item.ruta == rutaActual,
                            onClick = { onNavegar(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = null) },
                            label = { Text(item.etiqueta) }
                        )
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    contenido(PaddingValues())
                }
            }
        } else {
            contenido(padding)
        }
    }
}
