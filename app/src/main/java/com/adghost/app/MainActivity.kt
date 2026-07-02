package com.adghost.app

import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adghost.app.ui.screens.MainScreen
import com.adghost.app.ui.screens.UrlListScreen
import com.adghost.app.ui.theme.AdGhostTheme
import java.net.URLDecoder
import java.net.URLEncoder
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private var shouldAutoPip = false
    private var onAutoPipChanged: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onAutoPipChanged = { shouldAutoPip = it }

        setContent {
            AdGhostTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "url_list") {
                    composable("url_list") {
                        UrlListScreen(
                            onNavigateToBrowser = { url, nickname ->
                                val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                val encodedNickname = URLEncoder.encode(nickname, "UTF-8")
                                navController.navigate("browser/$encodedUrl/$encodedNickname")
                            }
                        )
                    }
                    composable(
                        route = "browser/{encodedUrl}/{encodedNickname}",
                        arguments = listOf(
                            navArgument("encodedUrl") { type = NavType.StringType },
                            navArgument("encodedNickname") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val url = URLDecoder.decode(
                            backStackEntry.arguments?.getString("encodedUrl") ?: "", "UTF-8"
                        )
                        val nickname = URLDecoder.decode(
                            backStackEntry.arguments?.getString("encodedNickname") ?: "", "UTF-8"
                        )
                        MainScreen(
                            url = url,
                            nickname = nickname,
                            onNavigateBack = { navController.popBackStack() },
                            onAutoPipChanged = onAutoPipChanged!!
                        )
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (shouldAutoPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Timber.d("MainActivity", "Auto PiP triggered")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } else {
                @Suppress("DEPRECATION")
                enterPictureInPictureMode()
            }
        }
    }
}
