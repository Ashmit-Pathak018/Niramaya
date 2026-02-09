package com.example.niramaya.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.niramaya.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    // --- STATE VARIABLES ---
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var history by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    // IMAGE STATES
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profilePicBase64 by remember { mutableStateOf("") } // The string version of image

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // --- 1. IMAGE PICKER LAUNCHER ---
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Convert to Base64 String immediately
            profilePicBase64 = uriToBase64(context, uri) ?: ""
        }
    }

    // --- FETCH EXISTING DATA ---
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

                        // Load existing image string if available
                        profilePicBase64 = document.getString("profilePic") ?: ""
                    }
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER
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
                text = "Edit Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF0F3D6E))
        } else {
            // --- PROFILE PIC SECTION ---
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.clickable { imageLauncher.launch("image/*") }) {
                if (selectedImageUri != null) {
                    // 1. Show newly picked image
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Profile Pic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                } else if (profilePicBase64.isNotEmpty()) {
                    // 2. Show saved image (Decode Base64)
                    val bitmap = base64ToBitmap(profilePicBase64)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Profile Pic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(100.dp).clip(CircleShape)
                        )
                    } else {
                        PlaceholderImage()
                    }
                } else {
                    // 3. Show Placeholder
                    PlaceholderImage()
                }

                // Edit Icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF0F3D6E), CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FORM FIELDS
            ProfileTextField("Full Name", name) { name = it }
            ProfileTextField("Phone Number", phone, isNumber = true) { phone = it }
            ProfileTextField("Age", age, isNumber = true) { age = it }
            ProfileTextField("Blood Group", bloodGroup) { bloodGroup = it }
            ProfileTextField("Gender", gender) { gender = it }
            ProfileTextField("Permanent Disease", history) { history = it }
            ProfileTextField("Allergies", allergies) { allergies = it }

            Spacer(modifier = Modifier.height(24.dp))

            // UPDATE BUTTON
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
                        "allergies" to allergies,
                        "profilePic" to profilePicBase64 // Saving the image string
                    )

                    db.collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            isSaving = false
                            Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// --- HELPER FUNCTIONS ---

@Composable
fun PlaceholderImage() {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Profile Pic",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)
    )
}

// Convert URI to Compressed Base64 String
fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val stream = ByteArrayOutputStream()
        // Compress heavily (quality 40) to keep Firestore happy
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream)
        val byteArrays = stream.toByteArray()
        Base64.encodeToString(byteArrays, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Convert String back to Bitmap
fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ProfileTextField(label: String, value: String, isNumber: Boolean = false, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEAF2F8),
                unfocusedContainerColor = Color(0xFFEAF2F8),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
        )
    }
}