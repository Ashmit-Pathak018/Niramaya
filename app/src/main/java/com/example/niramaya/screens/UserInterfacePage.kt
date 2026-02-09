package com.example.niramaya.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserInterfacePage(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var userName by remember { mutableStateOf("Loading...") }
    var userImageBase64 by remember { mutableStateOf("") }

    // --- FETCH DATA FROM FIRESTORE ---
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener {
                    userName = it.getString("fullName") ?: "User"
                    // Get the saved image string
                    userImageBase64 = it.getString("profilePic") ?: ""
                }
        }
    }

    // --- DECODE IMAGE LOGIC ---
    val profileBitmap = remember(userImageBase64) {
        if (userImageBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(24.dp)
    ) {
        // 1. Header Row
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
                text = "My Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Big Profile Pic & Name
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Display Bitmap if it exists, otherwise Placeholder
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

                // Edit Pencil Circle (Visual only here, clicking opens menu)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF0F3D6E), CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
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

        // 3. The Menu List

        // A. Profile -> Goes to the EDIT FORM
        MenuOptionItem(
            icon = Icons.Default.PersonOutline,
            text = "Profile",
            onClick = { navController.navigate("profile") } // Takes you to EditProfileScreen
        )

        // B. Important
        MenuOptionItem(
            icon = Icons.Default.FavoriteBorder,
            text = "Important",
            onClick = { Toast.makeText(navController.context, "Important Clicked", Toast.LENGTH_SHORT).show() }
        )

        // C. Settings
        MenuOptionItem(
            icon = Icons.Default.Settings,
            text = "Settings",
            onClick = { Toast.makeText(navController.context, "Settings Clicked", Toast.LENGTH_SHORT).show() }
        )

        // D. Help
        MenuOptionItem(
            icon = Icons.Default.HelpOutline,
            text = "Help",
            onClick = { Toast.makeText(navController.context, "Help Clicked", Toast.LENGTH_SHORT).show() }
        )

        // E. Logout
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
    }
}

// --- Helper Composable for Menu Rows ---
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
        // Icon Circle
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(Color(0xFFEAF2F8), CircleShape), // Light Blue Circle
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isDestructive) Color.Red else Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color.Red else Color.Black,
            modifier = Modifier.weight(1f)
        )

        // Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}