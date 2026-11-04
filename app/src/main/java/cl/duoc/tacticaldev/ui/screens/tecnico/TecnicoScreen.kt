package cl.duoc.tacticaldev.ui.screens.tecnico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.viewmodel.TecnicoViewModel

@Composable
fun TecnicoScreen(
    viewModel: TecnicoViewModel,
    modifier: Modifier = Modifier
) {
    val tecnicos = viewModel.tecnicoList.collectAsState().value

    var nombre by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del técnico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = cargo,
            onValueChange = { cargo = it },
            label = { Text("Cargo / Especialidad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.agregarTecnico(nombre, cargo)
                nombre = ""
                cargo = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar en Microservicio")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = tecnicos) { tecnico ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nombre: ${tecnico.nombre}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cargo: ${tecnico.cargo}",
                                style = MaterialTheme.typography.bodyMedium
                            )
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