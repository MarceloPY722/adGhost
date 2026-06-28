package com.adghost.app.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.adghost.app.util.NativeAdBlockEngine
import java.io.ByteArrayInputStream

class AdBlockWebViewClient(
    private val onPageStarted: (url: String?) -> Unit = {},
    private val onPageFinished: (url: String?) -> Unit = {},
    private val onProgressChanged: (progress: Int) -> Unit = {}
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished(url)
        view?.let { v ->
            v.evaluateJavascript(JsInjector.getEmergencyBlocker(), null)
            v.evaluateJavascript(JsInjector.getAdBlockScript(), null)
            v.evaluateJavascript(JsInjector.getCssHidingScript(), null)
        }
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        val sourceUrl = ""
        if (NativeAdBlockEngine.shouldBlockRequest(url, sourceUrl)) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }
        return null
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        val urlStr = url ?: return null
        if (NativeAdBlockEngine.shouldBlockRequest(urlStr, "")) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }
        return null
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        if (NativeAdBlockEngine.shouldBlockRequest(url, "")) {
            return true
        }
        if (url.startsWith("http") && !url.startsWith("https")) {
            return true
        }
        return false
    }
}