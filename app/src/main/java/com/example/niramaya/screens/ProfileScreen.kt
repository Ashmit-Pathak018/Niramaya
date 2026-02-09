package com.example.niramaya.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    // --- STATE VARIABLES (Hold the data) ---
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var history by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // --- FETCH EXISTING DATA ON LOAD ---
    LaunchedEffect(Unit) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("fullName") ?: ""
                        phone = document.getString("phone") ?: ""
                        age = document.getString("age") ?: ""
                        bloodGroup = document.getString("bloodGroup") ?: ""
                        gender = document.getString("gender") ?: ""
                        history = document.getString("medicalHistory") ?: ""
                        allergies = document.getString("allergies") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // --- UI CONTENT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5)) // Cream Background
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Make it scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. HEADER (Back Arrow + Title + Profile Pic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0F3D6E),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )

            // Title
            Text(
                text = "My Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E)
            )

            // Profile Pic with Edit Pencil
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder
                    contentDescription = "Profile Pic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                // Small Blue Edit Circle
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF0F3D6E), CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Icon",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF0F3D6E))
        } else {
            // 2. FORM FIELDS (Custom Style matches image)

            ProfileTextField("Full Name", name) { name = it }
            ProfileTextField("Phone Number", phone, isNumber = true) { phone = it }
            ProfileTextField("Age", age, isNumber = true) { age = it }
            // Date of Birth could be added here if needed
            ProfileTextField("Blood Group", bloodGroup) { bloodGroup = it }
            ProfileTextField("Gender", gender) { gender = it }

            // Email (Read Only usually, but editable here if you want)
            ProfileTextField("Email", email) { email = it }

            ProfileTextField("Permanent Disease / History", history) { history = it }
            ProfileTextField("Allergies", allergies) { allergies = it }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. UPDATE BUTTON
            Button(
                onClick = {
                    if (userId == null) return@Button
                    isSaving = true

                    val userMap = hashMapOf(
                        "fullName" to name,
                        "phone" to phone,
                        "age" to age,
                        "bloodGroup" to bloodGroup,
                        "gender" to gender,
                        "email" to email,
                        "medicalHistory" to history,
                        "allergies" to allergies
                    )

                    db.collection("users").document(userId)
                        .set(userMap) // .set() overwrites or creates
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack() // Go back to Home
                        }
                        .addOnFailureListener {
                            isSaving = false
                            Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)), // Dark Blue
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Update Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Extra spacing for bottom nav clearance
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- HELPER COMPONENT FOR THE INPUT FIELDS ---
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    isNumber: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEAF2F8), // Light Blue Background
                unfocusedContainerColor = Color(0xFFEAF2F8),
                disabledContainerColor = Color(0xFFEAF2F8),
                focusedIndicatorColor = Color.Transparent, // Remove underline
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF0F3D6E)
            ),
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            singleLine = true
        )
    }
}