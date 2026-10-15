package cl.duoc.tacticaldev.ui.screens.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.R
import cl.duoc.tacticaldev.data.local.DatosFicticios
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.ui.animaciones.sacudir
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.viewmodel.LoginViewModel

/** Pantalla de inicio de sesión con validación por campo. */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: (Rol) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "Logo Technical Fresh",
                modifier = Modifier.size(96.dp)
            )
            Text("TechnicalFresh", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Registro de visitas en terreno",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .sacudir(estado.intentosFallidos),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                Button(
                    onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                    enabled = !estado.cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    // Transición animada entre el texto y el indicador de carga
                    AnimatedContent(targetState = estado.cargando, label = "boton_login") { cargando ->
                        if (cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Ingresar")
                        }
                    }
                }
                Text(
                    "Usuarios de prueba: tecnico@demo.cl, vendedor@demo.cl, transportista@demo.cl " +
                        "o supervisor@demo.cl · clave ${DatosFicticios.CLAVE_DEMO}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
