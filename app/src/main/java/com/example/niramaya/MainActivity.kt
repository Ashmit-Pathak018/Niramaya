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

        // --- YOUR EXISTING ROUTES (UNCHANGED) ---

        composable("login") { LoginScreen(navController) }

        composable("home") { HomeScreen(navController) }

        // This remains your original User Interface Page
        composable("user_interface") { UserInterfacePage(navController) }

        // This remains your original Profile Route
        composable("profile") { ProfileScreen(navController) }

        composable("upload") { UploadScreen(navController) }

        composable("analysis_result") { AnalysisResultScreen(navController) }



        composable("emergency_qr") { EmergencyQRScreen(navController) }

        composable("history") { HistoryScreen(navController) }
        // Inside MainActivity.kt -> NavHost { ... }

        composable("schedule") { DoctorViewScreen(navController) }

        composable("select_medicines") {
            SelectMedicinesScreen(navController)
        }


        composable(
            route = "record_detail/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: return@composable
            RecordDetailScreen(navController = navController, recordId = recordId)
        }

        // --- NEW FEATURES ADDED (EXTRA ROUTES) ---

        // 1. Settings Screen
        composable("settings") { SettingsScreen(navController) }

        // 2. Important / Medical ID Screen
        composable("important") { ImportantScreen(navController) }

        // 3. Profile Update / Edit Screen
        composable("profile_update") { ProfileUpdateScreen(navController) }
    }
}