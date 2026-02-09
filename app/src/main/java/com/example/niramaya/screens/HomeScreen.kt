package com.example.niramaya.screens

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Patient") }
    var greeting by remember { mutableStateOf("Hi, Welcome Back") }
    var userImageBase64 by remember { mutableStateOf("") }

    // --- FETCH USER DATA ---
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("fullName") ?: userName
                    userImageBase64 = document.getString("profilePic") ?: ""
                }
        }
    }

    // --- DECODE PROFILE IMAGE ---
    val profileBitmap = remember(userImageBase64) {
        try {
            if (userImageBase64.isNotEmpty()) {
                val decoded = Base64.decode(userImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        containerColor = Color(0xFFFDF8F5),

        // ➕ FAB (Upload)
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("upload") },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(65.dp).offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        // 🔻 BOTTOM NAV BAR
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {

                // Home
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )

                // ✅ SCHEDULE → DOCTOR VIEW (PERSISTENT MEMORY)
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("doctor_view")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Doctor View",
                            tint = Color.Gray
                        )
                    }
                )

                // Profile
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("user_interface") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                    }
                )

                // History
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("history") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.history),
                            contentDescription = "History",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                    }
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            // HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap.asImageBitmap(),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("user_interface") }
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { navController.navigate("user_interface") }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(greeting, fontSize = 14.sp, color = Color(0xFF0F3D6E))
                    Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Niramaya",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2F8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Current Medication", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
