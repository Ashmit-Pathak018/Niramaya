package com.example.niramaya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.niramaya.screens.HomeScreen
import com.example.niramaya.screens.LoginScreen
import com.example.niramaya.screens.ProfileScreen
import com.example.niramaya.screens.UserInterfacePage
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. CHECK AUTH STATUS IMMEDIATELY
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // If user exists, go to Home. If not, go to Login.
        val startScreen = if (currentUser != null) "home" else "login"

        setContent {
            NiramayaApp(startDestination = startScreen)
        }
    }
}

@Composable
fun NiramayaApp(startDestination: String) {
    val navController = rememberNavController()

    // 2. USE THE DYNAMIC START DESTINATION
    NavHost(navController = navController, startDestination = startDestination) {

        // Screen 1: Login
        composable("login") {
            LoginScreen(navController)
        }

        // Screen 2: Home (Dashboard)
        composable("home") {
            HomeScreen(navController)
        }

        // Screen 3: User Interface (The Menu Hub)
        composable("user_interface") {
            UserInterfacePage(navController)
        }

        // Screen 4: Profile Form (The Editing Screen)
        composable("profile") {
            ProfileScreen(navController)
        }
    }
}