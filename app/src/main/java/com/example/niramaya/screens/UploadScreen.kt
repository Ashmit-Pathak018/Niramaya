package com.example.niramaya.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun UploadScreen(navController: NavController) {
    val context = LocalContext.current

    // --- STATE ---
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isDocument by remember { mutableStateOf(false) } // Track if it's a PDF/Doc
    var fileName by remember { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // --- LAUNCHERS ---

    // 1. Gallery (Images Only)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isDocument = false
            fileName = "Image Selected"
        }
    }

    // 2. Camera Result
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedUri = tempCameraUri
            isDocument = false
            fileName = "Camera Capture"
        }
    }

    // 3. Document Launcher (PDF, Word) - NEW!
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isDocument = true
            // Helper to get the real file name (e.g., "report.pdf")
            fileName = getFileNameFromUri(context, uri) ?: "Document Selected"
        }
    }

    // 4. Camera Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = composeFileProviderUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(24.dp)
    ) {
        // --- HEADER ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0F3D6E),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Text(
                text = "Upload Record",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- PREVIEW BOX ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { galleryLauncher.launch("image/*") }, // Default click action
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null) {
                if (isDocument) {
                    // SHOW FILE ICON for PDFs/Docs
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = "File",
                            tint = Color(0xFFE53935), // Red-ish for PDF feel
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = fileName,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    // SHOW IMAGE PREVIEW
                    Image(
                        painter = rememberAsyncImagePainter(selectedUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // EMPTY STATE
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Upload",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Image or File", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3 ACTION BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Camera
            ActionButton(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        val uri = composeFileProviderUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )

            // Gallery
            ActionButton(
                icon = Icons.Default.PhotoLibrary,
                label = "Gallery",
                onClick = { galleryLauncher.launch("image/*") }
            )

            // Files (NEW)
            ActionButton(
                icon = Icons.Default.InsertDriveFile,
                label = "Files",
                onClick = {
                    // Launch for PDF, Word, Text
                    documentLauncher.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- ANALYZE BUTTON ---
        Button(
            onClick = {
                if (selectedUri != null) {
                    isAnalyzing = true
                    // Fake AI Delay (2 seconds)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isAnalyzing = false
                        navController.navigate("analysis_result") // Navigate to new screen
                    }, 2000)
                } else {
                    Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(55.dp),
            enabled = selectedUri != null && !isAnalyzing
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Processing...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Analyze Record", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- HELPER COMPOSABLE FOR BUTTONS ---
@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFEAF2F8), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF0F3D6E))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color(0xFF0F3D6E), fontSize = 14.sp)
    }
}

// --- HELPER TO GET FILE NAME ---
fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor.use { c ->
            if (c != null && c.moveToFirst()) {
                val index = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = c.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

// --- HELPER TO CREATE CAMERA URI (Same as before) ---
fun composeFileProviderUri(context: Context): Uri {
    val directory = File(context.cacheDir, "images")
    directory.mkdirs()
    val file = File.createTempFile("selected_image_", ".jpg", directory)
    val authority = context.packageName + ".provider"
    return FileProvider.getUriForFile(context, authority, file)
}