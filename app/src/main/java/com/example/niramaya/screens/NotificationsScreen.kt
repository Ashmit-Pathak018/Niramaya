package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.data.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: return

    var alerts by remember { mutableStateOf<List<Appointment>>(emptyList()) }

    // Fetch upcoming appointments to show as notifications
    DisposableEffect(Unit) {
        val listener = db.collection("users").document(userId)
            .collection("appointments")
            .whereGreaterThan("timestamp", System.currentTimeMillis())
            .orderBy("timestamp")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    alerts = snap.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)
                    }
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        containerColor = Color(0xFFFDF8F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color(0xFF0F3D6E)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF0F3D6E))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFFDF8F5))
            )
        }
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No new notifications", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alerts) { apt ->
                    NotificationCard(apt)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(apt: Appointment) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE3F2FD), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text("Upcoming Appointment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Dr. ${apt.doctorName} on ${apt.dateStr} at ${apt.timeStr}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}