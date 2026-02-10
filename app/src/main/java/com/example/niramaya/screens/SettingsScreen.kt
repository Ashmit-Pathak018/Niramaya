package com.example.niramaya.screens

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.services.EmergencyService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    if (user == null) return

    // Reference to where we save the toggles
    val settingsRef = db.collection("users")
        .document(user.uid)
        .collection("settings")
        .document("preferences")

    // --- STATE ---
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emergencyEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // --- LOAD SETTINGS FROM FIREBASE ---
    LaunchedEffect(Unit) {
        settingsRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    notificationsEnabled = doc.getBoolean("pushNotifications") ?: true
                    emergencyEnabled = doc.getBoolean("emergencyNotifications") ?: false
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        containerColor = Color(0xFFFDF8F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold, color = Color(0xFF0F3D6E))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F3D6E)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFDF8F5)
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0F3D6E))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ---------- GENERAL ----------
                SettingsSectionTitle("General")

                // 1. Push Notifications Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        notificationsEnabled = enabled
                        // Save to Firebase so Service can check it
                        settingsRef.set(
                            mapOf("pushNotifications" to enabled),
                            SetOptions.merge()
                        )
                    }
                )

                // 2. Emergency Medical ID Toggle
                SettingsSwitchItem(
                    icon = Icons.Default.Warning,
                    title = "Emergency Medical ID",
                    checked = emergencyEnabled,
                    onCheckedChange = { enabled ->
                        emergencyEnabled = enabled

                        // Save Preference
                        settingsRef.set(
                            mapOf("emergencyNotifications" to enabled),
                            SetOptions.merge()
                        )

                        // Start / Stop Service Immediately
                        val intent = Intent(context, EmergencyService::class.java)
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            context.stopService(intent)
                        }
                    }
                )

                SettingsActionItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English"
                ) {
                    Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---------- SECURITY ----------
                SettingsSectionTitle("Security & Privacy")

                SettingsActionItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password"
                ) {
                    showPasswordDialog = true
                }

                SettingsActionItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy"
                ) {
                    Toast.makeText(context, "Privacy policy coming soon", Toast.LENGTH_SHORT).show()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---------- SUPPORT ----------
                SettingsSectionTitle("Support")

                SettingsActionItem(icon = Icons.Default.Help, title = "Help & Support") {}
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = "About Niramaya",
                    subtitle = "v1.0.0"
                ) {}

                Spacer(modifier = Modifier.height(32.dp))

                // ---------- DELETE ACCOUNT ----------
                Button(
                    onClick = {
                        Toast.makeText(context, "Contact support to delete account", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // ---------- PASSWORD RESET DIALOG ----------
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("Change Password") },
                text = { Text("Send password reset email to ${user.email}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            user.email?.let {
                                auth.sendPasswordResetEmail(it)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Email sent", Toast.LENGTH_LONG).show()
                                    }
                            }
                            showPasswordDialog = false
                        }
                    ) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/* ---------- REUSABLE UI ---------- */

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF0F3D6E))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), color = Color.Black)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0F3D6E))
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF0F3D6E))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.Black)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}