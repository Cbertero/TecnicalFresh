package cl.duoc.tacticaldev.ui.screens.admin

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.model.AdminUiState
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.ui.components.Selector
import cl.duoc.tacticaldev.ui.theme.Espaciado
import cl.duoc.tacticaldev.viewmodel.AdminViewModel

/** Formularios de administración: registrar puntos de venta y activos. */
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsState()
    val puntosVenta by viewModel.puntosVenta.collectAsState()

    Column(modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = estado.pestana) {
            Tab(
                selected = estado.pestana == 0,
                onClick = { viewModel.onPestanaChange(0) },
                text = { Text("Punto de venta") },
                icon = { Icon(Icons.Filled.Storefront, contentDescription = null) }
            )
            Tab(
                selected = estado.pestana == 1,
                onClick = { viewModel.onPestanaChange(1) },
                text = { Text("Activo") },
                icon = { Icon(Icons.Filled.AcUnit, contentDescription = null) }
            )
        }

        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = Espaciado.anchoMaximoFormulario)
                    .verticalScroll(rememberScrollState())
                    .padding(Espaciado.l),
                verticalArrangement = Arrangement.spacedBy(Espaciado.s)
            ) {
                if (estado.pestana == 0) {
                    FormularioPuntoVenta(estado, viewModel)
                } else {
                    FormularioActivo(estado, puntosVenta, viewModel)
                }
            }
        }
        Text(
            text = "TecnicalFresh • Gestión de Mantenimiento",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun FormularioPuntoVenta(estado: AdminUiState, viewModel: AdminViewModel) {
    val f = estado.puntoVenta
    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) viewModel.usarUbicacionActual() else viewModel.onPermisoUbicacionDenegado()
    }

    CampoTexto(f.nombre, viewModel::onNombrePuntoChange, "Nombre del local", Icons.Filled.Store, error = f.errores.nombre)
    CampoTexto(f.direccion, viewModel::onDireccionChange, "Dirección", Icons.Filled.Home, error = f.errores.direccion)
    CampoTexto(f.comuna, viewModel::onComunaChange, "Comuna", Icons.Filled.LocationCity, error = f.errores.comuna)
    Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.s)) {
        CampoTexto(
            f.latitud, viewModel::onLatitudChange, "Latitud", Icons.Filled.Straighten,
            modifier = Modifier.weight(1f),
            error = f.errores.latitud,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        CampoTexto(
            f.longitud, viewModel::onLongitudChange, "Longitud", Icons.Filled.Straighten,
            modifier = Modifier.weight(1f),
            error = f.errores.longitud,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
    OutlinedButton(
        onClick = {
            lanzadorPermisos.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        },
        enabled = !estado.obteniendoUbicacion,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (estado.obteniendoUbicacion) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Filled.MyLocation, contentDescription = null)
        }
        Text("Usar mi ubicación actual", modifier = Modifier.padding(start = Espaciado.s))
    }
    BotonGuardar("Registrar punto de venta", estado.guardando, viewModel::guardarPuntoVenta)
}

@Composable
private fun FormularioActivo(
    estado: AdminUiState,
    puntosVenta: List<PuntoVentaEntity>,
    viewModel: AdminViewModel
) {
    val f = estado.activo
    CampoTexto(
        f.codigo, viewModel::onCodigoChange, "Código del QR", Icons.Filled.QrCode,
        error = f.errores.codigo,
        ayuda = "Ejemplo: TDV-VF-004"
    )
    Selector(
        etiqueta = "Tipo de activo",
        opciones = TipoActivo.entries,
        seleccion = f.tipo,
        textoOpcion = { it.etiqueta },
        onSeleccion = viewModel::onTipoActivoChange,
        icono = Icons.Filled.Category,
        error = f.errores.tipo
    )
    CampoTexto(f.marca, viewModel::onMarcaChange, "Marca", Icons.Filled.Factory, error = f.errores.marca)
    CampoTexto(f.modelo, viewModel::onModeloChange, "Modelo", Icons.Filled.Tag, error = f.errores.modelo)
    Selector(
        etiqueta = "Punto de venta",
        opciones = puntosVenta,
        seleccion = puntosVenta.firstOrNull { it.id == f.puntoVentaId },
        textoOpcion = { "${it.nombre} · ${it.comuna}" },
        onSeleccion = { viewModel.onPuntoVentaChange(it.id) },
        icono = Icons.Filled.Storefront,
        error = f.errores.puntoVenta
    )
    BotonGuardar("Registrar activo", estado.guardando, viewModel::guardarActivo)
}

@Composable
private fun BotonGuardar(texto: String, guardando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !guardando,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Espaciado.s)
            .height(52.dp)
    ) {
        if (guardando) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Text(texto)
        }
    }
}
