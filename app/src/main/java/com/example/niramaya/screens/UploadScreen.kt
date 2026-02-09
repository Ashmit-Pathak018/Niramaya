package com.example.niramaya.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import com.example.niramaya.data.GeminiManager
import com.example.niramaya.data.TempAnalysisStore
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UploadScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- STATE ---
    val geminiManager = remember { GeminiManager() }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isDocument by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // --- LAUNCHERS ---

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            isDocument = false
            fileName = "Image Selected"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedUri = tempCameraUri
            isDocument = false
            fileName = "Camera Capture"
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedUri = it
            isDocument = true
            fileName = getFileNameFromUri(context, it) ?: "Document Selected"
        }
    }

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

        // --- PREVIEW ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedUri == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Image or File", color = Color.Gray)
                    }
                }

                isDocument -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.InsertDriveFile, null, tint = Color(0xFFE53935), modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            fileName,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                else -> {
                    Image(
                        painter = rememberAsyncImagePainter(selectedUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- ACTION BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButton(Icons.Default.CameraAlt, "Camera") {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (granted == PackageManager.PERMISSION_GRANTED) {
                    val uri = composeFileProviderUri(context)
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            ActionButton(Icons.Default.PhotoLibrary, "Gallery") {
                galleryLauncher.launch("image/*")
            }

            ActionButton(Icons.Default.InsertDriveFile, "Files") {
                documentLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- ANALYZE BUTTON ---
        Button(
            enabled = selectedUri != null && !isAnalyzing,
            onClick = {
                if (selectedUri == null) {
                    Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (isDocument) {
                    Toast.makeText(context, "PDF analysis coming soon. Use an image for demo.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isAnalyzing = true
                scope.launch {
                    try {
                        context.contentResolver.openInputStream(selectedUri!!)?.use { inputStream ->
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            if (bitmap == null) {
                                Toast.makeText(context, "Invalid image", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val result = geminiManager.analyzePrescription(bitmap)
                            if (result.isNullOrBlank()) {
                                Toast.makeText(context, "AI could not extract data", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            // ✅ SAFE HANDOFF
                            TempAnalysisStore.jsonResult = result
                            navController.navigate("analysis_result")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, e.message ?: "Processing failed", Toast.LENGTH_LONG).show()
                    } finally {
                        isAnalyzing = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Analyzing with Gemini...", fontWeight = FontWeight.Bold)
            } else {
                Text("Analyze Record", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- HELPERS ---

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(60.dp).background(Color(0xFFEAF2F8), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF0F3D6E))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color(0xFF0F3D6E), fontSize = 14.sp)
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) return it.getString(index)
            }
        }
    }
    return uri.path?.substringAfterLast('/')
}

fun composeFileProviderUri(context: Context): Uri {
    val directory = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("camera_img_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
