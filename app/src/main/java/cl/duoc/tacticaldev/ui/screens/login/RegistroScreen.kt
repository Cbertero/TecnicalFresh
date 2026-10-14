package cl.duoc.tacticaldev.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val cargosOperario = listOf("Técnico de mantenimiento", "Vendedora", "Transportista")

    TacticalScaffold(
        titulo = "Crear Cuenta",
        itemsNavegacion = emptyList(),
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro de Personal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea una cuenta para acceder al sistema en terreno",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                // Campo Nombre
                CampoTexto(
                    valor = estado.nombre,
                    onValorChange = viewModel::onNombreChange,
                    etiqueta = "Nombre completo",
                    icono = Icons.Default.Person,
                    error = estado.errorNombre,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Campo Correo
                CampoTexto(
                    valor = estado.correo,
                    onValorChange = viewModel::onCorreoChange,
                    etiqueta = "Correo electrónico",
                    icono = Icons.Default.Email,
                    error = estado.errorCorreo,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )

                // Selector de Rol (Operario o Supervisor)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = estado.rol == Rol.OPERARIO,
                        onClick = { viewModel.onRolChange(Rol.OPERARIO) },
                        label = { Text("Operario") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = estado.rol == Rol.SUPERVISOR,
                        onClick = { viewModel.onRolChange(Rol.SUPERVISOR) },
                        label = { Text("Supervisor") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Si es Operario, selector de cargo (Técnico, Vendedora, Transportista)
                if (estado.rol == Rol.OPERARIO) {
                    Text(
                        text = "Especialidad en terreno:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cargosOperario.forEach { cargo ->
                            FilterChip(
                                selected = estado.cargo == cargo,
                                onClick = { viewModel.onCargoChange(cargo) },
                                label = { Text(cargo.split(" ").first(), style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Campo Contraseña
                CampoTexto(
                    valor = estado.clave,
                    onValorChange = viewModel::onClaveChange,
                    etiqueta = "Contraseña",
                    icono = Icons.Default.Lock,
                    error = estado.errorClave,
                    visualTransformation = if (estado.claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    trailingIcon = {
                        IconButton(onClick = viewModel::toggleClaveVisible) {
                            Icon(
                                if (estado.claveVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                // Campo Confirmar Contraseña
                CampoTexto(
                    valor = estado.confirmarClave,
                    onValorChange = viewModel::onConfirmarClaveChange,
                    etiqueta = "Confirmar contraseña",
                    icono = Icons.Default.Lock,
                    error = estado.errorConfirmarClave,
                    visualTransformation = if (estado.claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )

                // Error general si el correo ya existe
                AnimatedVisibility(
                    visible = estado.errorGeneral != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            estado.errorGeneral.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Botón Registrarse
                Button(
                    onClick = { viewModel.registrar(onRegistroExitoso) },
                    enabled = !estado.cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (estado.cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Crear Cuenta")
                    }
                }
            }

            // Footer corporativo al fondo
            Text(
                text = "TecnicalFresh • Gestión de Mantenimiento",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}