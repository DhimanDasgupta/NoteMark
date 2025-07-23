package com.dhimandasgupta.notemark.ui.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dhimandasgupta.notemark.app.nav.NoteMarkRoot
import com.dhimandasgupta.notemark.ui.designsystem.NoteMarkTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            val actualActivity = LocalActivity.current
            val windowSizeClass = calculateWindowSizeClass(actualActivity as Activity)

            NoteMarkTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    NoteMarkRoot(
                        navController = navController,
                        windowSizeClass = windowSizeClass,
                        modifier = Modifier.Companion
                            .consumeWindowInsets(paddingValues = innerPadding)
                    )
                }
            }
        }
    }
}