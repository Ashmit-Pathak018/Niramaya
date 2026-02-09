package com.example.niramaya.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // 🔥 USER DATA
    var name by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var disease by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 FETCH FROM FIREBASE
    LaunchedEffect(Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                name = doc.getString("fullName") ?: "Patient"
                bloodGroup = doc.getString("bloodGroup") ?: "--"
                allergies = doc.getString("allergies") ?: "None"
                disease = doc.getString("disease") ?: "None"
                emergencyName = doc.getString("emergencyContactName") ?: "Not Set"
                emergencyPhone = doc.getString("emergencyContactNumber") ?: ""
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Important Records", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
                shape = RoundedCornerShape(16.dp)
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
                        fontSize = 14.sp
                    )
                }
            }

            // 🆔 MEDICAL ID CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color(0xFF0F3D6E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medical ID", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            bloodGroup,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Patient", fontSize = 13.sp, color = Color.Gray)

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Known Allergies", fontWeight = FontWeight.Medium)
                    Text(allergies, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Chronic Conditions", fontWeight = FontWeight.Medium)
                    Text(disease, color = Color.DarkGray)
                }
            }

            // ⚡ QUICK ACTIONS
            Text("Quick Actions", fontWeight = FontWeight.Bold)

            // 📞 CALL EMERGENCY CONTACT
            ActionCard(
                title = "Call Emergency Contact",
                subtitle = emergencyName,
                color = Color(0xFFE3F2FD)
            ) {
                if (emergencyPhone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$emergencyPhone")
                    }
                    navController.context.startActivity(intent)
                }
            }

            // 🚑 CALL AMBULANCE
            ActionCard(
                title = "Call Ambulance",
                subtitle = "112",
                color = Color(0xFFFFEBEE),
                isDanger = true
            ) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:112")
                }
                navController.context.startActivity(intent)
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    color: Color,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Call,
                contentDescription = null,
                tint = if (isDanger) Color.Red else Color(0xFF0F3D6E)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
