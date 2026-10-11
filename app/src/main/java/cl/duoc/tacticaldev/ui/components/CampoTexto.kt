package cl.duoc.tacticaldev.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto reutilizable con ícono, mensaje de error (isError + supportingText)
 * y texto de ayuda. Mantiene la validación visual igual en todos los formularios.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    sufijo: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = when {
            trailingIcon != null -> trailingIcon
            error != null -> {
                { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
            }
            else -> null
        },
        suffix = if (sufijo != null) {
            { Text(sufijo) }
        } else {
            null
        },
        isError = error != null,
        supportingText = when {
            error != null -> { { Text(error) } }
            ayuda != null -> { { Text(ayuda) } }
            else -> null
        },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}
