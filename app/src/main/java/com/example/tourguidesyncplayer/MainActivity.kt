// パス: app/src/main/java/com/example/tourguidesyncplayer/MainActivity.kt
package com.example.tourguidesyncplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tourguidesyncplayer.ui.guide.GuideScreen
import com.example.tourguidesyncplayer.ui.launch.LaunchScreen
import com.example.tourguidesyncplayer.ui.theme.TourGuideSyncPlayerTheme
import com.example.tourguidesyncplayer.ui.user.UserScreen // 修正箇所
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourGuideSyncPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "launch") {
        composable("launch") {
            LaunchScreen(
                onGuideClick = { navController.navigate("guide") },
                onUserClick = { navController.navigate("user") }
            )
        }
        composable("guide") {
            GuideScreen(navController = navController)
        }
        composable("user") {
            UserScreen(navController = navController)
        }
    }
}