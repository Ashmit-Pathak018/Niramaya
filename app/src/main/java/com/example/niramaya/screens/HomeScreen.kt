package com.example.niramaya.screens

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

    // --- STATE VARIABLES ---
    var userName by remember { mutableStateOf("Patient") }
    var greeting by remember { mutableStateOf("Hi, Welcome Back") }

    // --- FETCH DATA ---
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName")
                        if (!fullName.isNullOrEmpty()) {
                            userName = fullName
                        }
                    }
                }
        }
    }

    // --- UI STRUCTURE (SCAFFOLD) ---
    Scaffold(
        containerColor = Color(0xFFFDF8F5), // Your Cream Background

        // 1. FLOATING ACTION BUTTON (The Blue Upload Button)
        floatingActionButton = {
            FloatingActionButton(
                onClick = { Toast.makeText(context, "Upload Coming Soon", Toast.LENGTH_SHORT).show() },
                containerColor = Color(0xFF2196F3), // Bright Blue
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(65.dp).offset(y = (-10).dp) // Move it up slightly
            ) {
                Icon(
                    imageVector = Icons.Default.Upload, // Or use a specific upload icon if you have one
                    contentDescription = "Upload",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center, // Puts it in the middle

        // 2. BOTTOM NAVIGATION BAR
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                // Home Item
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already Home */ },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.home), contentDescription = "Home", modifier = Modifier.size(24.dp))
                    },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFE3F2FD))
                )
                // Schedule Item
                NavigationBarItem(
                    selected = false,
                    onClick = { Toast.makeText(context, "Schedule Clicked", Toast.LENGTH_SHORT).show() },
                    icon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Schedule", tint = Color.Gray)
                    }
                )
                // Profile Item (UPDATED: Now goes to User Interface Menu)
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("user_interface") }, // <--- CHANGED THIS
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.profile), contentDescription = "Profile", modifier = Modifier.size(24.dp), tint = Color.Gray)
                    }
                )
                // History Item
                NavigationBarItem(
                    selected = false,
                    onClick = { Toast.makeText(context, "History Clicked", Toast.LENGTH_SHORT).show() },
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.outline_archive_24), contentDescription = "History", modifier = Modifier.size(24.dp), tint = Color.Gray)
                    }
                )
            }
        }
    ) { paddingValues ->

        // --- MAIN CONTENT AREA ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Respects the bottom bar space
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // 3. HEADER ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Pic (UPDATED: Now goes to User Interface Menu)
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace if you have a real user pic
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { navController.navigate("user_interface") } // <--- CHANGED THIS
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Welcome
                Column(modifier = Modifier.weight(1f)) {
                    Text(greeting, color = Color(0xFF0F3D6E), fontSize = 14.sp)
                    Text(userName, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Bell Icon
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            // "Niramaya" Title
            Text(
                text = "Niramaya",
                color = Color(0xFF0F3D6E),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            // 4. CURRENT MEDICATION CARD
            Card(
                shape = RoundedCornerShape(32.dp), // Extra rounded corners as per design
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2F8)), // Light Blue
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Current Medication",
                        color = Color(0xFF0F3D6E),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // The Blue Dot & Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("•", color = Color(0xFF0F3D6E), fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. APPOINTMENT SECTION
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Left: Text Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2F8)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Your next doctor visit is with Dr. Ashmit Pathak on 10th February",
                            color = Color(0xFF0F3D6E),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Text(
                            "2 days remaining",
                            color = Color(0xFFFF5722), // Orange
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Doctor Illustration
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.doctorview), // YOUR SVG/PNG IMAGE
                        contentDescription = "Doctor Illustration",
                        contentScale = ContentScale.Fit, // Fits the image nicely
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}