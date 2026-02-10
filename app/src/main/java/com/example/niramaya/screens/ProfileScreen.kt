package com.example.niramaya.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Person
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
import com.example.niramaya.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    // --- STATE ---
    var userProfile by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- FETCH & DECRYPT DATA ---
    LaunchedEffect(Unit) {
        FirestoreRepository.getUserProfile(
            onSuccess = { data ->
                userProfile = data
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    val fullName = userProfile["fullName"] ?: "Loading..."
    val email = auth.currentUser?.email ?: ""
    val phone = userProfile["phoneNumber"] ?: "Not set"
    val blood = userProfile["bloodGroup"] ?: "-"
    val age = userProfile["age"] ?: "-"
    val gender = userProfile["gender"] ?: "-"
    val profilePicBase64 = userProfile["profilePic"] ?: ""

    Scaffold(
        containerColor = Color(0xFFFDF8F5),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("profile_update") },
                containerColor = Color(0xFF0F3D6E),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, "Edit Profile")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F3D6E),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Medical ID",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3D6E)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF0F3D6E))
            } else {
                // --- PROFILE IMAGE ---
                Box(contentAlignment = Alignment.BottomEnd) {
                    val bitmap = remember(profilePicBase64) {
                        try {
                            if (profilePicBase64.isNotEmpty()) {
                                val bytes = Base64.decode(profilePicBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else null
                        } catch (e: Exception) { null }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF0F3D6E), CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Placeholder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(email, fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(30.dp))

                // --- INFO CARDS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoCard(label = "Blood", value = blood, icon = Icons.Default.Bloodtype)
                    InfoCard(label = "Age", value = age, icon = Icons.Default.Person)
                    InfoCard(label = "Gender", value = gender, icon = Icons.Default.Person)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CONTACT DETAILS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRow(icon = Icons.Default.LocalPhone, label = "Phone", value = phone)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                        ProfileRow(icon = Icons.Default.Email, label = "Email", value = email)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 24.dp)
            .width(80.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF0F3D6E), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFEAF2F8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF0F3D6E), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}