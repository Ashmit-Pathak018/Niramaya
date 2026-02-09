package com.example.niramaya

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.niramaya.screens.*
import com.example.niramaya.services.EmergencyService
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. NOTIFICATION PERMISSION (Android 13+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        // --- 2. START EMERGENCY SERVICE ---
        val serviceIntent = Intent(this, EmergencyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // --- 3. CHECK LOGIN STATUS ---
        val auth = FirebaseAuth.getInstance()
        val startDest = if (auth.currentUser != null) "home" else "login"

        // --- 4. LOAD UI ---
        setContent {
            NiramayaApp(startDestination = startDest)
        }
    }
}

@Composable
fun NiramayaApp(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // Login
        composable("login") {
            LoginScreen(navController)
        }

        // Home
        composable("home") {
            HomeScreen(navController)
        }

        // User Menu
        composable("user_interface") {
            UserInterfacePage(navController)
        }

        // Profile
        composable("profile") {
            ProfileScreen(navController)
        }

        // Upload
        composable("upload") {
            UploadScreen(navController)
        }

        // Analysis (uses TempAnalysisStore, no args)
        composable("analysis_result") {
            AnalysisResultScreen(navController)
        }

        composable("doctor_view") {
            DoctorViewScreen(navController)
        }


        // Emergency QR
        composable("emergency_qr") {
            EmergencyQRScreen(navController)
        }

        // History
        composable("history") {
            HistoryScreen(navController)
        }

        // ✅ Record Detail (EDIT EXISTING RECORD)
        composable(
            route = "record_detail/{recordId}",
            arguments = listOf(
                navArgument("recordId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val recordId =
                backStackEntry.arguments?.getString("recordId") ?: return@composable

            RecordDetailScreen(
                navController = navController,
                recordId = recordId
            )
        }
    }
}
