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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
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
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUpdateScreen(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // ---------- FORM STATE ----------
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var disease by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }

    // Emergency
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }

    // Profile picture
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profilePicBase64 by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    // ---------- IMAGE PICKER ----------
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            profilePicBase64 = uriToBase64(context, uri)
        }
    }

    // ---------- LOAD EXISTING DATA ----------
    LaunchedEffect(Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                fullName = doc.getString("fullName") ?: ""
                phoneNumber = doc.getString("phoneNumber") ?: ""
                age = doc.getString("age") ?: ""
                dob = doc.getString("dob") ?: ""
                bloodGroup = doc.getString("bloodGroup") ?: ""
                gender = doc.getString("gender") ?: ""
                disease = doc.getString("disease") ?: ""
                allergies = doc.getString("allergies") ?: ""

                emergencyName = doc.getString("emergencyContactName") ?: ""
                emergencyPhone = doc.getString("emergencyContactNumber") ?: ""

                profilePicBase64 = doc.getString("profilePic") ?: ""
            }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color(0xFF0F3D6E))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F3D6E)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- PROFILE PIC ----------
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { imagePicker.launch("image/*") }
            ) {

                val bitmap = remember(profilePicBase64) {
                    base64ToBitmap(profilePicBase64)
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Profile Pic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(110.dp).clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Placeholder",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0F3D6E), CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- PERSONAL DETAILS ----------
            SectionHeader("Personal Details")
            CustomTextField("Full Name", fullName) { fullName = it }
            CustomTextField("Phone Number", phoneNumber) { phoneNumber = it }
            CustomTextField("Age", age) { age = it }
            CustomTextField("Date of Birth", dob, "DD / MM / YYYY") { dob = it }
            CustomTextField("Blood Group", bloodGroup, "O+") { bloodGroup = it }
            CustomTextField("Gender", gender) { gender = it }

            Text("Email", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = email,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE3F2FD),
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- MEDICAL ----------
            SectionHeader("Medical History")
            CustomTextField("Permanent Disease", disease) { disease = it }
            CustomTextField("Allergies", allergies) { allergies = it }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- EMERGENCY ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Contact", fontWeight = FontWeight.Bold, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(12.dp))
            EmergencyTextField("Contact Name", emergencyName) { emergencyName = it }
            EmergencyTextField("Contact Phone", emergencyPhone) { emergencyPhone = it }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- SAVE ----------
            Button(
                onClick = {
                    isSaving = true

                    val data = mapOf(
                        "fullName" to fullName,
                        "phoneNumber" to phoneNumber,
                        "age" to age,
                        "dob" to dob,
                        "bloodGroup" to bloodGroup,
                        "gender" to gender,
                        "disease" to disease,
                        "allergies" to allergies,
                        "emergencyContactName" to emergencyName,
                        "emergencyContactNumber" to emergencyPhone,
                        "profilePic" to profilePicBase64
                    )

                    db.collection("users").document(userId)
                        .update(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { isSaving = false }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Update Profile")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/* ---------- HELPERS ---------- */

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F3D6E),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun CustomTextField(
    label: String,
    value: String,
    placeholder: String = "",
    onChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE3F2FD),
                unfocusedContainerColor = Color(0xFFE3F2FD),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun EmergencyTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, fontWeight = FontWeight.Bold, color = Color.Red)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFEBEE),
                unfocusedContainerColor = Color(0xFFFFEBEE),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

/* ---------- IMAGE UTILS ---------- */

private fun uriToBase64(context: Context, uri: Uri): String {
    val input = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(input)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream)
    return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
}

private fun base64ToBitmap(base64: String): Bitmap? {
    if (base64.isEmpty()) return null
    return try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}
