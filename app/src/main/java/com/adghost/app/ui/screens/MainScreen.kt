package com.adghost.app.ui.screens

import android.content.Intent
import android.util.Log
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adghost.app.ui.components.AdBlockWebView
import timber.log.Timber

@Composable
fun MainScreen(
    url: String,
    nickname: String,
    onNavigateBack: () -> Unit
) {
    var currentUrl by rememberSaveable { mutableStateOf(url) }
    var pageTitle by rememberSaveable { mutableStateOf(nickname) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var webCanGoBack by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current

    var isFullscreen by remember { mutableStateOf(false) }

    BackHandler(enabled = isFullscreen || webCanGoBack) {
        if (isFullscreen) {
            webViewRef?.let { wv ->
                wv.webChromeClient?.onHideCustomView()
            }
        } else {
            webViewRef?.goBack()
        }
    }

    Timber.d("MainScreen", "Composing MainScreen for URL: $currentUrl, title: $pageTitle")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Text(
                                text = pageTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    Timber.d("MainScreen", "Share clicked: $currentUrl")
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, currentUrl)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir"))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            LinearProgressIndicator(
                                progress = progress / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AdBlockWebView(
                    url = currentUrl,
                    onWebViewCreated = { webView ->
                        Timber.d("MainScreen", "WebView created")
                        webViewRef = webView
                    },
                    onProgressChanged = { p ->
                        progress = p
                        isLoading = p < 100
                    },
                    onPageTitleChanged = { title ->
                        if (title.isNotEmpty()) {
                            Timber.d("MainScreen", "Page title changed: $title")
                            pageTitle = title
                        }
                    },
                    onUrlChanged = { newUrl ->
                        if (newUrl.isNotEmpty()) {
                            Timber.d("MainScreen", "URL changed: $newUrl")
                            currentUrl = newUrl
                        }
                    },
                    onCanGoBackChanged = { canGoBack ->
                        Timber.d("MainScreen", "Can go back changed: $canGoBack")
                        webCanGoBack = canGoBack
                    },
                    onCanGoForwardChanged = { },
                    onFullscreenChanged = { isFs ->
                        isFullscreen = isFs
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = !isFullscreen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 32.dp, end = 32.dp),
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { 
                            Timber.d("MainScreen", "Home clicked, reloading: $currentUrl")
                            webViewRef?.loadUrl(currentUrl) 
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }

                    FilledIconButton(
                        onClick = { 
                            Timber.d("MainScreen", "Refresh clicked")
                            webViewRef?.reload() 
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }

                    FilledIconButton(
                        onClick = {
                            Timber.d("MainScreen", "Open in browser: $currentUrl")
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(currentUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Abrir en navegador"
                        )
                    }
                }
            }
        }
    }
}