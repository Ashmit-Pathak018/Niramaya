package com.example.niramaya.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.R
import com.example.niramaya.data.FirestoreRepository // 🔥 Added Repository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserInterfacePage(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    // --- STATE ---
    var userName by remember { mutableStateOf("Loading...") }
    var userImageBase64 by remember { mutableStateOf("") }

    // --- FETCH & DECRYPT DATA ---
    LaunchedEffect(Unit) {
        // We use the Repository so it automatically Decrypts the name
        FirestoreRepository.getUserProfile(
            onSuccess = { data ->
                userName = data["fullName"] ?: "User"
                userImageBase64 = data["profilePic"] ?: ""
            },
            onFailure = {
                userName = "User"
            }
        )
    }

    // --- DECODE IMAGE ---
    val profileBitmap = remember(userImageBase64) {
        try {
            if (userImageBase64.isNotEmpty()) {
                val decodedBytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(24.dp)
    ) {

        // HEADER
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0F3D6E),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // PROFILE IMAGE + NAME
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap.asImageBitmap(),
                    contentDescription = "Profile Pic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Profile Pic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // MENU OPTIONS

        // 1. Profile (Goes to View Screen)
        MenuOptionItem(
            icon = Icons.Default.PersonOutline,
            text = "Profile",
            onClick = { navController.navigate("profile") } // ✅ Correct Route
        )

        // 2. Important (Medical ID / Emergency)
        MenuOptionItem(
            icon = Icons.Default.FavoriteBorder,
            text = "Important Info",
            onClick = { navController.navigate("important") }
        )

        // 3. Settings (General)
        MenuOptionItem(
            icon = Icons.Default.Settings,
            text = "Settings",
            onClick = { navController.navigate("settings") }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 4. Logout
        MenuOptionItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            text = "Logout",
            isDestructive = true,
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// --- MENU ITEM COMPONENT ---
@Composable
fun MenuOptionItem(
    icon: ImageVector,
    text: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFEAF2F8),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isDestructive) Color.Red else Color(0xFF0F3D6E),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = if (isDestructive) Color.Red else Color.Black
        )

        // Arrow
        if (!isDestructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}