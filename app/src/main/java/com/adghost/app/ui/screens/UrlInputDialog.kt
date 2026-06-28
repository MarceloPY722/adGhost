package com.adghost.app.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adghost.app.util.PreferencesManager

@Composable
fun UrlInputDialog(
    initialSite: PreferencesManager.SavedSite?,
    onDismiss: () -> Unit,
    onSave: (nickname: String, url: String) -> Unit
) {
    val context = LocalContext.current
    var nickname by remember { mutableStateOf(initialSite?.nickname ?: "") }
    var url by remember { mutableStateOf(initialSite?.url ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSite != null) "Editar sitio" else "Agregar sitio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it; error = null },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; error = null },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            url = clip.getItemAt(0).text?.toString() ?: url
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Pegar desde portapapeles")
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nickname.isBlank()) {
                    error = "El nombre no puede estar vacío"
                    return@TextButton
                }
                if (url.isBlank()) {
                    error = "La URL no puede estar vacía"
                    return@TextButton
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                onSave(nickname.trim(), url.trim())
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
