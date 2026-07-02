package com.adghost.app.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.adghost.app.webview.AdBlockWebViewClient
import com.adghost.app.webview.JsInjector

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AdBlockWebView(
    url: String,
    isAudioMode: Boolean = false,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {},
    onProgressChanged: (progress: Int) -> Unit = {},
    onPageTitleChanged: (title: String) -> Unit = {},
    onUrlChanged: (url: String) -> Unit = {},
    onCanGoBackChanged: (Boolean) -> Unit = {},
    onCanGoForwardChanged: (Boolean) -> Unit = {},
    onFullscreenChanged: (Boolean) -> Unit = {}
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(isAudioMode) {
        val wv = webViewRef.value
        if (wv != null && wv.url != null && wv.url!!.isNotEmpty()) {
            if (isAudioMode) {
                wv.evaluateJavascript(JsInjector.getAudioModeScript(), null)
            } else {
                wv.evaluateJavascript(JsInjector.getVideoModeScript(), null)
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val fullscreenContainer = FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            var customView: View? = null
            var customViewCallback: WebChromeClient.CustomViewCallback? = null
            val activity = context as? Activity

            val webView = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = false
                    allowContentAccess = false
                    javaScriptCanOpenWindowsAutomatically = false
                    mediaPlaybackRequiresUserGesture = false
                }
                webViewClient = AdBlockWebViewClient(
                    onPageStarted = { onUrlChanged(it ?: "") },
                    onPageFinished = {
                        onPageTitleChanged(title ?: "")
                        onCanGoBackChanged(canGoBack())
                        onCanGoForwardChanged(canGoForward())
                    },
                    onProgressChanged = onProgressChanged
                )
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                    }
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        onPageTitleChanged(title ?: "")
                    }
                    override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                        result?.confirm()
                        return true
                    }
                    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                        result?.confirm()
                        return true
                    }
                    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                        result?.confirm()
                        return true
                    }
                    override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
                        if (customView != null) {
                            callback?.onCustomViewHidden()
                            return
                        }
                        customView = view
                        customViewCallback = callback
                        fullscreenContainer.addView(view, FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ))
                        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        activity?.window?.decorView?.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
                        onFullscreenChanged(true)
                    }
                    override fun onHideCustomView() {
                        customView?.let { fullscreenContainer.removeView(it) }
                        customView = null
                        customViewCallback?.onCustomViewHidden()
                        customViewCallback = null
                        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                        onFullscreenChanged(false)
                    }
                }
            }

            fullscreenContainer.addView(webView, 0)
            webView.loadUrl(url)
            webViewRef.value = webView
            onWebViewCreated(webView)
            fullscreenContainer
        },
        update = { container ->
            if (container.childCount > 0) {
                val wv = container.getChildAt(0) as? WebView
                if (wv != null && wv.url != url && url.isNotEmpty()) {
                    wv.loadUrl(url)
                }
            }
        }
    )
}