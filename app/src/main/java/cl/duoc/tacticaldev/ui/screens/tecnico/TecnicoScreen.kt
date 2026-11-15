package cl.duoc.tacticaldev.ui.screens.tecnico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.viewmodel.TecnicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoScreen(
    viewModel: TecnicoViewModel,
    modifier: Modifier = Modifier
) {
    val tecnicos by viewModel.tecnicoList.collectAsState()
    val operarios by viewModel.operarios.collectAsState()
    val locales by viewModel.locales.collectAsState()
    val mensajeEstado by viewModel.mensajeEstado.collectAsState()
    val sincronizando by viewModel.sincronizando.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var localAsignado by remember { mutableStateOf("") }

    var menuOperarioAbierto by remember { mutableStateOf(false) }
    var menuLocalAbierto by remember { mutableStateOf(false) }

    // Seleccionar valores por defecto cuando cargan las listas si aún están vacíos
    LaunchedEffect(operarios) {
        if (nombre.isBlank() && operarios.isNotEmpty()) {
            val primero = operarios.first()
            nombre = primero.nombre
            cargo = primero.cargo.ifBlank { "Operario" }
        }
    }
    LaunchedEffect(locales) {
        if (localAsignado.isBlank() && locales.isNotEmpty()) {
            localAsignado = locales.first().nombre
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Barra de estado Online / Offline + Botón Sincronizar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = mensajeEstado ?: "Asignación de locales a personal en terreno",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { viewModel.fetchTecnicos() },
                    enabled = !sincronizando
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sincronizar con servidor"
                    )
                }
            }
        }

        // Selector 1: Elegir Trabajador registrado (Técnico, Vendedor, Transportista, etc.)
        ExposedDropdownMenuBox(
            expanded = menuOperarioAbierto,
            onExpandedChange = { menuOperarioAbierto = !menuOperarioAbierto }
        ) {
            OutlinedTextField(
                value = if (nombre.isNotBlank()) "$nombre ($cargo)" else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Seleccionar trabajador del equipo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOperarioAbierto)
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = menuOperarioAbierto,
                onDismissRequest = { menuOperarioAbierto = false }
            ) {
                operarios.forEach { operario ->
                    val especialidad = operario.cargo.ifBlank { "Operario" }
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(operario.nombre, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = especialidad,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            nombre = operario.nombre
                            cargo = especialidad
                            menuOperarioAbierto = false
                        }
                    )
                }
            }
        }

        // Selector 2: Elegir Local / Punto de Venta a revisar
        ExposedDropdownMenuBox(
            expanded = menuLocalAbierto,
            onExpandedChange = { menuLocalAbierto = !menuLocalAbierto }
        ) {
            OutlinedTextField(
                value = localAsignado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Local comercial a revisar") },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuLocalAbierto)
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = menuLocalAbierto,
                onDismissRequest = { menuLocalAbierto = false }
            ) {
                locales.forEach { punto ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(punto.nombre, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${punto.direccion} • ${punto.comuna}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            localAsignado = punto.nombre
                            menuLocalAbierto = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                viewModel.agregarTecnico(nombre, cargo, localAsignado)
            },
            enabled = nombre.isNotBlank() && localAsignado.isNotBlank() && !sincronizando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (sincronizando) "Guardando..." else "Asignar Local al Trabajador")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = "Asignaciones registradas (${tecnicos.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = tecnicos) { tecnico ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = tecnico.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cargo: ${tecnico.cargo}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Local asignado: ${tecnico.localAsignado?.ifBlank { "Sin especificar" } ?: "Sin especificar"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Etiqueta de sincronización Online / Offline
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (tecnico.sincronizado) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (tecnico.sincronizado) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (tecnico.sincronizado) {
                                        "Sincronizado en Servidor (MySQL)"
                                    } else {
                                        "Guardado en Local (Pendiente de Internet)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (tecnico.sincronizado) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                        }
                        if (tecnico.id != null) {
                            IconButton(onClick = { viewModel.eliminarTecnico(tecnico.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
