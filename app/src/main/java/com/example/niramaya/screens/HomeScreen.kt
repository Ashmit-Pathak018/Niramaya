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

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    // ---------------- STATE ----------------
    var userName by remember { mutableStateOf("Patient") }
    var userImageBase64 by remember { mutableStateOf("") }

    var activeMeds by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isMedsLoading by remember { mutableStateOf(true) }

    // ---------------- FETCH USER + MEDS ----------------
    LaunchedEffect(Unit) {
        if (user != null) {

            // 🔥 LIVE USER PROFILE
            db.collection("users")
                .document(user.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        userName = doc.getString("fullName") ?: userName
                        userImageBase64 = doc.getString("profilePic") ?: ""
                    }
                }

            // 🔥 LIVE ACTIVE MEDICATIONS
            db.collection("users")
                .document(user.uid)
                .collection("active_medications")
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        activeMeds = snap.documents.mapNotNull {
                            val name = it.getString("name")
                            val dosage = it.getString("dosage")
                            if (name != null && dosage != null) name to dosage else null
                        }
                        isMedsLoading = false
                    }
                }
        }
    }

    // ---------------- PROFILE IMAGE ----------------
    val profileBitmap = remember(userImageBase64) {
        try {
            if (userImageBase64.isNotEmpty()) {
                val bytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ---------------- UI ----------------
    Scaffold(
        containerColor = Color(0xFFFDF8F5),

        // FAB
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

        // BOTTOM NAV
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("schedule") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Schedule",
                            tint = Color.Gray
                        )
                    }
                )

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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            // ---------------- HEADER ----------------
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
                    Text("Hi, Welcome Back", fontSize = 14.sp, color = Color(0xFF0F3D6E))
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
                "Niramaya",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------- CURRENT MEDICATION ----------------
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2F8)),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        "Current Medication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F3D6E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isMedsLoading -> {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }

                        activeMeds.isEmpty() -> {
                            Text("No active medications", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Select medicines from prescriptions",
                                color = Color(0xFF2196F3),
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate("select_medicines")
                                }
                            )
                        }

                        else -> {
                            activeMeds.take(3).forEach { (name, dosage) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("•", fontSize = 24.sp, color = Color(0xFF0F3D6E))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("$name — $dosage", fontSize = 14.sp)
                                }
                            }

                            if (activeMeds.size > 3) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "+${activeMeds.size - 3} more",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------- APPOINTMENT + BANNER ----------------
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2F8)),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Next doctor visit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F3D6E)
                        )

                        Text(
                            "Check your schedule for upcoming appointments",
                            fontSize = 13.sp,
                            color = Color(0xFF0F3D6E)
                        )

                        Text(
                            "View Schedule →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.clickable {
                                navController.navigate("schedule")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.home_banner),
                    contentDescription = "Home Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                )
            }
        }
    }
}
