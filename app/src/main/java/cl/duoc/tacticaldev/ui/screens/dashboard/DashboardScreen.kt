package cl.duoc.tacticaldev.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.ui.theme.SuperficieTarjeta
import cl.duoc.tacticaldev.ui.theme.TextoBlancoPuro
import cl.duoc.tacticaldev.ui.theme.TextoSecundarioGris

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    nombreUsuario: String,
    rolUsuario: String,
    onIrAActivos: () -> Unit,
    onIrAMisVisitas: () -> Unit,
    onEscanearQr: () -> Unit,
    onCerrarSesion: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier
) {
    // Estado para controlar el diálogo de confirmación
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    // Diálogo de confirmación antes de cerrar sesión
    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoSalir = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogoSalir = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TecnicalFresh",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,) }

            )
        },
        bottomBar = {
            Text(
                text = "TecnicalFresh • Gestión de Mantenimiento",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Encabezado de Usuario (Avatar + Saludo + Cargo)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0C4A6E),
                    border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF38BDF8)
                        )
                    }
                }
                Text(
                    text = "¡Hola, $nombreUsuario!",
                    style = MaterialTheme.typography.headlineSmall
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = rolUsuario.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. Cuadrícula de opciones principales (2 x 2)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Fila 1
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BotonHub(
                        titulo = "Vitrinas / Activos",
                        subtitulo = "Ver inventario",
                        icono = Icons.Default.Kitchen,
                        colorBorde = Color(0xFF22D3EE), // Cian hielo
                        onClick = onIrAActivos,
                        modifier = Modifier.weight(1f)
                    )
                    BotonHub(
                        titulo = "Mis Visitas",
                        subtitulo = "Historial",
                        icono = Icons.Default.Assignment,
                        colorBorde = Color(0xFF10B981), // Verde esmeralda
                        onClick = onIrAMisVisitas,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fila 2
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BotonHub(
                        titulo = "Escanear QR",
                        subtitulo = "Ingreso rápido",
                        icono = Icons.Default.QrCodeScanner,
                        colorBorde = Color(0xFF818CF8), // Índigo hielo
                        onClick = onEscanearQr,
                        modifier = Modifier.weight(1f)
                    )
                    BotonHub(
                        titulo = "Mi Perfil",
                        subtitulo = "Cerrar sesión",
                        icono = Icons.Default.Person,
                        colorBorde = Color(0xFFFB7185), // Pizarra sutil
                        onClick = { mostrarDialogoSalir = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonHub(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorBorde: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuperficieTarjeta
        ),
        border = BorderStroke(1.dp, colorBorde.copy(alpha = 0.35f)), // Borde luminoso sutil
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colorBorde.copy(alpha = 0.12f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = colorBorde
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = TextoBlancoPuro
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundarioGris
            )
        }
    }
}

