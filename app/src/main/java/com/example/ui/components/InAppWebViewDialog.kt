package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppWebViewDialog(
    url: String,
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var progress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }

    val displayUrl = remember(url) {
        if (url.contains("archive.org/details/")) {
            url.replace("archive.org/details/", "archive.org/embed/")
        } else {
            url
        }
    }

    // Intercept back presses to go back in the WebView history instead of dismissing the dialog
    BackHandler(enabled = canGoBack) {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !canGoBack
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Back Button (for Web history)
                        IconButton(
                            onClick = {
                                webView?.let {
                                    if (it.canGoBack()) {
                                        it.goBack()
                                    }
                                }
                            },
                            enabled = canGoBack
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "पीछे जाएँ",
                                tint = if (canGoBack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        // Title
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = url,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Refresh Button
                        IconButton(
                            onClick = { webView?.reload() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "पुनः लोड करें",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Open in Browser fallback
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ब्राउज़र खोलने में असमर्थ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "ब्राउज़र में खोलें",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "बंद करें",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Smooth linear progress indicator
                if (progress < 100) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                }

                // WebView container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("in_app_webview"),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    // Custom user agent to prevent archive.org from forcing mobile redirect app popups
                                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        canGoBack = view?.canGoBack() ?: false
                                    }
 
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        overrideUrl: String?
                                    ): Boolean {
                                        if (overrideUrl != null && (overrideUrl.startsWith("http://") || overrideUrl.startsWith("https://"))) {
                                            val finalUrl = if (overrideUrl.contains("archive.org/details/")) {
                                                overrideUrl.replace("archive.org/details/", "archive.org/embed/")
                                            } else {
                                                overrideUrl
                                            }
                                            view?.loadUrl(finalUrl)
                                            return true
                                        }
                                        return false
                                    }
                                }
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        progress = newProgress
                                        canGoBack = view?.canGoBack() ?: false
                                    }
                                }
                                loadUrl(displayUrl)
                                webView = this
                            }
                        },
                        update = { view ->
                            // Ensure the URL remains correct if recomposed
                            webView = view
                        }
                    )
                }
            }
        }
    }
}
