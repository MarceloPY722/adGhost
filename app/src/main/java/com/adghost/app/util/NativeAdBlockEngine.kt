package com.adghost.app.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

object NativeAdBlockEngine {
    private const val TAG = "NativeAdBlockEngine"

    @Volatile
    private var initialized = false
    private var initError: String? = null

    init {
        try {
            System.loadLibrary("adghost_adblock")
            Timber.d(TAG, "Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Timber.e(e, TAG, "Failed to load native library")
            initError = e.message
        }
    }

    private external fun nativeInit(data: ByteArray): Int
    private external fun nativeShouldBlock(url: String, sourceUrl: String, requestType: String): Boolean
    private external fun nativeDestroy()

    fun init(context: Context) {
        if (initialized) {
            Timber.d(TAG, "Already initialized")
            return
        }
        if (initError != null) {
            Timber.e(TAG, "Previous init error: $initError")
            return
        }

        Timber.d(TAG, "Starting async init...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d(TAG, "Loading precompiled engine data...")
                val data = context.assets.open("adblock_engine.dat").use { it.readBytes() }
                Timber.d(TAG, "Engine data loaded: ${data.size} bytes")

                Timber.d(TAG, "Calling nativeInit...")
                val result = nativeInit(data)
                Timber.d(TAG, "nativeInit returned: $result")

                if (result == 0) {
                    initialized = true
                    Timber.d(TAG, "NativeAdBlockEngine initialized successfully")
                } else {
                    initError = "nativeInit failed with code: $result"
                    Timber.e(TAG, initError)
                }
            } catch (e: Exception) {
                initError = e.message ?: "Unknown error"
                Timber.e(e, TAG, "Failed to initialize native engine: $initError")
            }
        }
    }

    fun shouldBlockRequest(url: String, sourceUrl: String = ""): Boolean {
        if (initError != null) {
            return false
        }
        if (!initialized) {
            return false
        }
        return try {
            nativeShouldBlock(url, sourceUrl, "other")
        } catch (e: Exception) {
            false
        }
    }

    fun destroy() {
        try {
            nativeDestroy()
        } catch (_: Exception) { }
        initialized = false
    }
}