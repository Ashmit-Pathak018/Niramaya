package com.example.niramaya

import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.example.niramaya.services.PushNotificationService // 🔥 Added Import
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

        // --- 2. CREATE NOTIFICATION CHANNEL (REQUIRED) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "appointment_channel",
                "Appointments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming doctor visits"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // --- 3. START EMERGENCY SERVICE (LOCK SCREEN) ---
        val emergencyIntent = Intent(this, EmergencyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(emergencyIntent)
        } else {
            startService(emergencyIntent)
        }

        // --- 4. START PUSH NOTIFICATION SERVICE (ALERTS) ---
        // This listens for new medicine/doctor alerts in the background
        val pushIntent = Intent(this, PushNotificationService::class.java)
        startService(pushIntent)

        // --- 5. CHECK LOGIN STATUS ---
        val auth = FirebaseAuth.getInstance()
        val startDest = if (auth.currentUser != null) "home" else "login"

        // --- 6. LOAD UI ---
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

        // --- AUTH & HOME ---
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }

        // --- MAIN FEATURES ---
        composable("user_interface") { UserInterfacePage(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("upload") { UploadScreen(navController) }
        composable("analysis_result") { AnalysisResultScreen(navController) }
        composable("emergency_qr") { EmergencyQRScreen(navController) }
        composable("history") { HistoryScreen(navController) }

        // --- SCHEDULE & DOCTOR ---
        composable("schedule") { ScheduleScreen(navController) }
        composable("doctor_view") { DoctorViewScreen(navController) }

        // --- NOTIFICATIONS ---
        composable("notifications") { NotificationsScreen(navController) }

        // --- MEDICINES & RECORDS ---
        composable("select_medicines") { SelectMedicinesScreen(navController) }

        composable(
            route = "record_detail/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: return@composable
            RecordDetailScreen(navController = navController, recordId = recordId)
        }

        // --- SETTINGS & EXTRAS ---
        composable("settings") { SettingsScreen(navController) }
        composable("important") { ImportantScreen(navController) }
        composable("profile_update") { ProfileUpdateScreen(navController) }
    }
}