package cl.duoc.tacticaldev.ui.screens.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.R
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.ui.animaciones.sacudir
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.ui.theme.AmbarAlerta
import cl.duoc.tacticaldev.ui.theme.AmbarAlertaContenedor
import cl.duoc.tacticaldev.ui.theme.BordeCianGlacial
import cl.duoc.tacticaldev.ui.theme.CianContenedor
import cl.duoc.tacticaldev.ui.theme.TextoBlancoPuro
import cl.duoc.tacticaldev.ui.theme.TextoSecundarioGris
import cl.duoc.tacticaldev.ui.theme.VerdeExito
import cl.duoc.tacticaldev.ui.theme.VerdeExitoContenedor
import cl.duoc.tacticaldev.viewmodel.LoginViewModel

/** Fondo de la cabecera: siempre oscuro, en modo claro y en modo oscuro. */
private val FondoCabecera = Color(0xFF0B1422)

/** Pantalla de inicio de sesión: cabecera con el logo y tarjeta con el formulario. */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: (Rol) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndicadorServidor(
                    enLinea = estado.servidorEnLinea,
                    onReintentar = viewModel::comprobarServidor
                )
                Text(
                    "v1.0 · Bertero / Durán · 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // 1. Cabecera oscura con el logo y el nombre
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(FondoCabecera)
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(CianContenedor)
                            .border(2.dp, BordeCianGlacial, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo),
                            contentDescription = "Logo Technical Fresh",
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Text(
                        "Technical Fresh",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoPuro
                    )
                    Text(
                        "Visitas verificadas en terreno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundarioGris
                    )
                }

                // 2. Tarjeta del formulario, superpuesta a la cabecera
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 224.dp, start = 20.dp, end = 20.dp)
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .sacudir(estado.intentosFallidos)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Bienvenido",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Ingresa para registrar tus visitas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        CampoTexto(
                            valor = estado.correo,
                            onValorChange = viewModel::onCorreoChange,
                            etiqueta = "Correo",
                            icono = Icons.Filled.Email,
                            error = estado.errores.correo,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )
                        CampoTexto(
                            valor = estado.clave,
                            onValorChange = viewModel::onClaveChange,
                            etiqueta = "Clave",
                            icono = Icons.Filled.Lock,
                            error = estado.errores.clave,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            visualTransformation = if (estado.claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = viewModel::alternarVisibilidadClave) {
                                    Icon(
                                        if (estado.claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (estado.claveVisible) "Ocultar clave" else "Mostrar clave"
                                    )
                                }
                            }
                        )

                        // El mensaje de error aparece y desaparece con animación
                        AnimatedVisibility(
                            visible = estado.errorGeneral != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            // Sin conexión (ámbar) se distingue de una clave incorrecta (rojo)
                            if (estado.servidorEnLinea == false) {
                                MensajeLogin(
                                    icono = Icons.Filled.CloudOff,
                                    titulo = "Sin conexión",
                                    detalle = "Necesitas internet para iniciar sesión. Revisa tu conexión e inténtalo de nuevo.",
                                    fondo = AmbarAlertaContenedor
                                )
                            } else {
                                MensajeLogin(
                                    icono = Icons.Filled.ErrorOutline,
                                    titulo = "No pudimos iniciar sesión",
                                    detalle = estado.errorGeneral.orEmpty(),
                                    fondo = MaterialTheme.colorScheme.errorContainer
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                            enabled = !estado.cargando,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(52.dp)
                        ) {
                            // Transición animada entre el texto y el indicador de carga
                            AnimatedContent(targetState = estado.cargando, label = "boton_login") { cargando ->
                                if (cargando) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Ingresar", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Indicador del estado del microservicio.
 * null = comprobando · true = en línea · false = sin conexión (se puede tocar para reintentar).
 */
@Composable
private fun IndicadorServidor(enLinea: Boolean?, onReintentar: () -> Unit) {
    val (texto, punto, fondo) = when (enLinea) {
        null -> Triple("Comprobando servidor…", MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant)
        true -> Triple("Servidor en línea", VerdeExito, VerdeExitoContenedor)
        false -> Triple("Sin conexión con el servidor · Reintentar", AmbarAlerta, AmbarAlertaContenedor)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .clickable(enabled = enLinea == false, onClick = onReintentar)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(punto)
        )
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Aviso del login: ícono, título en negrita y una línea de detalle. */
@Composable
private fun MensajeLogin(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    fondo: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}
