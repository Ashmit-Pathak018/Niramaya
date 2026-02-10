package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.data.FirestoreRepository
import com.example.niramaya.data.HistoryRecord
import com.example.niramaya.summary.GeminiSummaryService
import com.example.niramaya.utils.formatGeminiSummary

// --- COLORS ---
val PrimaryBlue = Color(0xFF0F3D6E)
val BackgroundBeige = Color(0xFFFDF8F5)
val CardWhite = Color.White
val AccentBlueLight = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorViewScreen(navController: NavController) {

    var records by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
    var aiSummary by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    // 🔥 1. Listen to Live Records
    LaunchedEffect(Unit) {
        FirestoreRepository.listenToRecords {
            records = it
        }
    }

    // 🤖 2. Generate AI Summary (Triggered when records load)
    LaunchedEffect(records) {
        if (records.isNotEmpty()) {
            isAiLoading = true
            try {
                // Calls the service with the prompt that matches your Formatter
                aiSummary = GeminiSummaryService().generateDoctorSummary(records)
            } catch (e: Exception) {
                aiSummary = "Unable to generate summary at this time."
            } finally {
                isAiLoading = false
            }
        }
    }

    Scaffold(
        containerColor = BackgroundBeige,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Doctor Summary",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundBeige
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // --- SECTION 1: AI MEDICAL SUMMARY ---
            item {
                Text(
                    "AI Generated Insights",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isAiLoading -> LoadingCard()
                    aiSummary != null -> GeminiSummaryCard(aiSummary!!)
                    else -> { /* Hidden if no data */ }
                }
            }

            // --- SECTION 2: RECENT RECORDS ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Recent Records",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryBlue
                )
            }

            if (records.isEmpty()) {
                item {
                    Text(
                        "No records found.",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                items(records.take(5)) { record ->
                    RecordItemCard(record)
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

// --- UI COMPONENT: AI SUMMARY CARD ---
@Composable
fun GeminiSummaryCard(rawText: String) {
    // This splits the raw AI text into sections using your utils/GeminiTextFormatter.kt
    val sections = remember(rawText) { formatGeminiSummary(rawText) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header with Magic Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color(0xFF9C27B0) // Purple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Medical Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = Color(0xFFEEEEEE)
            )

            // Dynamic Sections
            SummarySectionRow(
                icon = Icons.Default.Assignment,
                title = "Patient Summary",
                content = sections.patientSummary
            )

            SummarySectionRow(
                icon = Icons.Default.MedicalServices,
                title = "Active Medications",
                content = sections.medications
            )

            SummarySectionRow(
                icon = Icons.Default.Description,
                title = "Important Findings",
                content = sections.findings
            )

            if (sections.alerts.isNotBlank()) {
                SummarySectionRow(
                    icon = Icons.Default.Warning,
                    title = "Alerts",
                    content = sections.alerts,
                    isAlert = true
                )
            }
        }
    }
}

// --- UI COMPONENT: SINGLE SUMMARY ROW ---
@Composable
fun SummarySectionRow(
    icon: ImageVector,
    title: String,
    content: String,
    isAlert: Boolean = false
) {
    if (content.isBlank()) return

    Row(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Colored Icon Box
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isAlert) Color(0xFFFFEBEE) else AccentBlueLight,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) Color.Red else PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (isAlert) Color.Red else Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            val cleanContent = content.replace("*", "").trim()

            cleanContent.lines().forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isNotBlank()) {
                    Row(modifier = Modifier.padding(top = 2.dp)) {
                        Text("•", color = Color.Gray, modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = trimmedLine.removePrefix("-").trim(),
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// --- UI COMPONENT: RECORD ITEM ---
@Composable
fun RecordItemCard(record: HistoryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Logic
            val icon = if (record.title.contains("Prescription", ignoreCase = true)) {
                Icons.Default.MedicalServices
            } else {
                Icons.Default.Description
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title.ifBlank { "Medical Record" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryBlue
                )
                Text(
                    text = record.recordType.name.replace("_", " ").lowercase().capitalize(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

// --- UI COMPONENT: LOADING STATE ---
@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryBlue)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Analyzing medical records...", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

// Helper for Record Type string formatting
fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }