package com.example.niramaya.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // 🔥 FIXED: Defaults are now empty strings, not "Not Set"
    var name by remember { mutableStateOf("Patient") }
    var bloodGroup by remember { mutableStateOf("--") }
    var allergies by remember { mutableStateOf("None") }
    var disease by remember { mutableStateOf("None") }
    var emergencyName by remember { mutableStateOf("") } // Was "Not Set" -> Caused the bug
    var emergencyPhone by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }

    // 🔥 FETCH & DECRYPT DATA
    LaunchedEffect(Unit) {
        FirestoreRepository.getUserProfile(
            onSuccess = { data ->
                name = data["fullName"] ?: "Patient"
                bloodGroup = data["bloodGroup"] ?: "--"
                allergies = data["allergies"] ?: "None"
                disease = data["disease"] ?: "None"
                emergencyName = data["emergencyContactName"] ?: ""
                emergencyPhone = data["emergencyContactNumber"] ?: ""
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Important Records", fontWeight = FontWeight.Bold, color = Color(0xFF0F3D6E))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F3D6E)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF8F4)
                )
            )
        },
        containerColor = Color(0xFFFFF8F4)
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0F3D6E))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // 🚨 WARNING BANNER
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEAEA)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Show this screen to doctors or first responders in case of emergency.",
                            color = Color.Red,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // 🆔 MEDICAL ID CARD
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE3F2FD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color(0xFF0F3D6E)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Medical ID", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F3D6E))
                            Spacer(modifier = Modifier.weight(1f))

                            // Blood Group Badge
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = bloodGroup,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Patient Name", fontSize = 12.sp, color = Color.Gray)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Known Allergies", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F3D6E))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(allergies, color = Color.DarkGray, fontSize = 15.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Conditions", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F3D6E))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(disease, color = Color.DarkGray, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ⚡ QUICK ACTIONS
                Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F3D6E))

                // 📞 CALL EMERGENCY CONTACT (FIXED LOGIC)
                val isContactSet = emergencyName.isNotBlank() && emergencyPhone.isNotBlank()

                ActionCard(
                    title = if (isContactSet) "Call Emergency Contact" else "Set Emergency Contact",
                    subtitle = if (isContactSet) "$emergencyName ($emergencyPhone)" else "Tap to configure now",
                    color = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF0F3D6E)
                ) {
                    if (isContactSet) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$emergencyPhone")
                        }
                        context.startActivity(intent)
                    } else {
                        // Redirect to edit profile if missing
                        navController.navigate("profile_update")
                    }
                }

                // 🚑 CALL AMBULANCE
                ActionCard(
                    title = "Call Ambulance",
                    subtitle = "112 (Emergency Services)",
                    color = Color(0xFFFFEBEE),
                    iconColor = Color.Red
                ) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:112")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    color: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = null,
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}