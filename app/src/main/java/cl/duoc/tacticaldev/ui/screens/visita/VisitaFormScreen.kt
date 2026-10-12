package cl.duoc.tacticaldev.ui.screens.visita

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.ActivoConPunto
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.model.VisitaFormUiState
import cl.duoc.tacticaldev.model.tipoActivo
import cl.duoc.tacticaldev.ui.animaciones.sacudir
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.ui.components.FotoEvidencia
import cl.duoc.tacticaldev.ui.components.Selector
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.theme.AmbarAlerta
import cl.duoc.tacticaldev.ui.theme.AmbarAlertaContenedor
import cl.duoc.tacticaldev.ui.theme.Espaciado
import cl.duoc.tacticaldev.ui.theme.VerdeExito
import cl.duoc.tacticaldev.ui.theme.VerdeExitoContenedor
import cl.duoc.tacticaldev.utils.Formatos
import cl.duoc.tacticaldev.utils.FotoUtils
import cl.duoc.tacticaldev.utils.GeoUtils
import cl.duoc.tacticaldev.viewmodel.VisitaViewModel

/** Formulario de registro de una visita al activo seleccionado. */
@Composable
fun VisitaFormScreen(
    viewModel: VisitaViewModel,
    onVolver: () -> Unit,
    onVisitaGuardada: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado derivado: cuántos de los 6 requisitos están completos (Guía 12)
    val progreso by remember {
        derivedStateOf {
            val s = estado
            listOf(
                s.tipoVisita != null,
                s.estadoEquipo != null,
                s.temperatura.isNotBlank(),
                s.fotoUri != null,
                s.latitud != null,
                s.confirmado
            ).count { it } / 6f
        }
    }
    val progresoAnimado by animateFloatAsState(targetValue = progreso, label = "progreso_formulario")

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    TacticalScaffold(
        titulo = "Registrar visita",
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    ) { padding ->
        if (estado.cargando) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@TacticalScaffold
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = Espaciado.anchoMaximoFormulario)
                    .verticalScroll(rememberScrollState())
                    .padding(Espaciado.l),
                verticalArrangement = Arrangement.spacedBy(Espaciado.m)
            ) {
                estado.activo?.let { ResumenActivo(it) }

                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    modifier = Modifier.fillMaxWidth()
                )

                Selector(
                    etiqueta = "Tipo de visita",
                    opciones = TipoVisita.entries,
                    seleccion = estado.tipoVisita,
                    textoOpcion = { it.etiqueta },
                    onSeleccion = viewModel::onTipoVisitaChange,
                    icono = Icons.AutoMirrored.Filled.Assignment,
                    error = estado.errores.tipoVisita
                )

                SelectorEstadoEquipo(
                    seleccion = estado.estadoEquipo,
                    error = estado.errores.estadoEquipo,
                    onSeleccion = viewModel::onEstadoEquipoChange
                )

                CampoTexto(
                    valor = estado.temperatura,
                    onValorChange = viewModel::onTemperaturaChange,
                    etiqueta = "Temperatura medida",
                    icono = Icons.Filled.Thermostat,
                    error = estado.errores.temperatura,
                    ayuda = "Entre ${Validadores.TEMPERATURA_MIN.toInt()} y ${Validadores.TEMPERATURA_MAX.toInt()} °C",
                    sufijo = "°C",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Aviso animado: con falla u observaciones, describir el problema es obligatorio
                AnimatedVisibility(
                    visible = estado.estadoEquipo != null && estado.estadoEquipo != EstadoEquipo.OPERATIVO,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        "Describe en las observaciones el problema detectado",
                        color = AmbarAlerta,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                CampoTexto(
                    valor = estado.observaciones,
                    onValorChange = viewModel::onObservacionesChange,
                    etiqueta = "Observaciones",
                    icono = Icons.AutoMirrored.Filled.Notes,
                    error = estado.errores.observaciones,
                    ayuda = "${estado.observaciones.length}/${Validadores.OBSERVACIONES_MAX}",
                    singleLine = false,
                    minLines = 3
                )

                SeccionEvidencia(estado, onFotoCapturada = viewModel::onFotoCapturada)

                SeccionUbicacion(
                    estado = estado,
                    onObtenerUbicacion = viewModel::obtenerUbicacion,
                    onPermisoDenegado = viewModel::onPermisoUbicacionDenegado
                )

                FilaConfirmacion(
                    confirmado = estado.confirmado,
                    error = estado.errores.confirmacion,
                    onCambio = viewModel::onConfirmacionChange
                )

                Button(
                    onClick = { viewModel.guardar(onVisitaGuardada) },
                    enabled = !estado.guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .sacudir(estado.intentosFallidos)
                ) {
                    if (estado.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar visita")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenActivo(activo: ActivoConPunto) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("${activo.codigo} · ${activo.tipoActivo.etiqueta}", style = MaterialTheme.typography.titleMedium)
            Text("${activo.puntoVentaNombre} · ${activo.direccion}, ${activo.comuna}")
            Text(
                "La fecha y hora se registran automáticamente al guardar",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SelectorEstadoEquipo(
    seleccion: EstadoEquipo?,
    error: String?,
    onSeleccion: (EstadoEquipo) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        Text("Estado del equipo", style = MaterialTheme.typography.titleSmall)
        EstadoEquipo.entries.forEach { estadoEquipo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = estadoEquipo == seleccion,
                        onClick = { onSeleccion(estadoEquipo) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 2.dp)
            ) {
                RadioButton(selected = estadoEquipo == seleccion, onClick = null)
                Icon(
                    iconoEstado(estadoEquipo),
                    contentDescription = null,
                    tint = colorEstado(estadoEquipo),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(estadoEquipo.etiqueta)
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun iconoEstado(estado: EstadoEquipo): ImageVector = when (estado) {
    EstadoEquipo.OPERATIVO -> Icons.Filled.CheckCircle
    EstadoEquipo.CON_OBSERVACIONES -> Icons.Filled.Warning
    EstadoEquipo.CON_FALLA -> Icons.Filled.Error
}

@Composable
private fun colorEstado(estado: EstadoEquipo) = when (estado) {
    EstadoEquipo.OPERATIVO -> VerdeExito
    EstadoEquipo.CON_OBSERVACIONES -> AmbarAlerta
    EstadoEquipo.CON_FALLA -> MaterialTheme.colorScheme.error
}

/**
 * Evidencia fotográfica de la revisión: pide el permiso de cámara y usa
 * TakePicture con un URI de FileProvider (Guía 13). Solo se permite la cámara,
 * no la galería, para que la foto sea tomada en el momento de la visita.
 */
@Composable
private fun SeccionEvidencia(estado: VisitaFormUiState, onFotoCapturada: (String) -> Unit) {
    val context = LocalContext.current
    // rememberSaveable: si Android destruye la actividad mientras la cámara está abierta
    // (poca memoria o rotación), el URI pendiente no se pierde al volver.
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val uri = uriPendiente
        if (exito && uri != null) {
            onFotoCapturada(uri)
        } else if (uri != null) {
            // Cancelado: se elimina el archivo vacío para no acumular basura
            FotoUtils.eliminar(context, Uri.parse(uri))
        }
        uriPendiente = null
    }

    fun abrirCamara() {
        val uri = FotoUtils.crearUriFoto(context)
        uriPendiente = uri.toString()
        lanzadorCamara.launch(uri)
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoDenegado = !concedido
        if (concedido) abrirCamara()
    }

    SeccionConIcono(
        titulo = "Evidencia fotográfica",
        icono = Icons.Filled.PhotoCamera,
        error = estado.errores.foto
    ) {
        FotoEvidencia(
            uri = estado.fotoUri,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
        )
        if (permisoDenegado) {
            Text(
                "Sin permiso de cámara no se puede registrar la evidencia",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        OutlinedButton(
            onClick = {
                val tienePermiso = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (tienePermiso) abrirCamara() else lanzadorPermiso.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
            Text(
                if (estado.fotoUri == null) "Tomar foto" else "Tomar otra foto",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Ubicación donde se realiza la visita: pide el permiso de ubicación,
 * obtiene las coordenadas y muestra la distancia al punto de venta.
 */
@Composable
private fun SeccionUbicacion(
    estado: VisitaFormUiState,
    onObtenerUbicacion: () -> Unit,
    onPermisoDenegado: () -> Unit
) {
    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) onObtenerUbicacion() else onPermisoDenegado()
    }

    SeccionConIcono(
        titulo = "Ubicación de la visita",
        icono = Icons.Filled.LocationOn,
        error = estado.errores.ubicacion
    ) {
        val distancia = estado.distanciaMetros
        if (estado.latitud != null && estado.longitud != null && distancia != null) {
            Text(
                "Lat ${"%.5f".format(estado.latitud)} · Lon ${"%.5f".format(estado.longitud)}",
                style = MaterialTheme.typography.bodySmall
            )
            InsigniaVerificacion(verificada = estado.ubicacionVerificada, distancia = distancia)
        } else {
            Text("Registra tu ubicación para verificar que estás en el punto de venta")
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
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Obteniendo ubicación...", modifier = Modifier.padding(start = 8.dp))
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Text(
                    if (estado.latitud == null) "Obtener ubicación" else "Actualizar ubicación",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/** Muestra si la ubicación está dentro del radio permitido del punto de venta. */
@Composable
private fun InsigniaVerificacion(verificada: Boolean, distancia: Double) {
    // El color cambia de forma animada al pasar de "fuera de rango" a "verificada"
    val fondo by animateColorAsState(if (verificada) VerdeExitoContenedor else AmbarAlertaContenedor, label = "fondo_gps")
    val texto by animateColorAsState(if (verificada) VerdeExito else AmbarAlerta, label = "texto_gps")
    Surface(color = fondo, shape = MaterialTheme.shapes.small) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                if (verificada) Icons.Filled.VerifiedUser else Icons.Filled.WrongLocation,
                contentDescription = null,
                tint = texto
            )
            Text(
                if (verificada) {
                    "Ubicación verificada · a ${Formatos.distancia(distancia)} del punto"
                } else {
                    "Fuera de rango · a ${Formatos.distancia(distancia)} (máx. ${GeoUtils.RADIO_VERIFICACION_METROS.toInt()} m)"
                },
                color = texto,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun SeccionConIcono(
    titulo: String,
    icono: ImageVector,
    error: String?,
    contenido: @Composable () -> Unit
) {
    OutlinedCard(
        border = if (error != null) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(titulo, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp))
            }
            contenido()
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FilaConfirmacion(confirmado: Boolean, error: String?, onCambio: (Boolean) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = onCambio)
            Text("Confirmo que estuve en el punto de venta y que la información es verídica")
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
