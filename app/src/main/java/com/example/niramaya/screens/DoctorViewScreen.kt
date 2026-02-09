package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.data.FirestoreRepository
import com.example.niramaya.data.HistoryRecord
import com.example.niramaya.summary.GeminiSummaryService
import com.example.niramaya.utils.formatGeminiSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorViewScreen(navController: NavController) {

    var records by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
    var aiSummary by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    // 🔥 Firestore stays untouched
    LaunchedEffect(Unit) {
        FirestoreRepository.listenToRecords {
            records = it
        }
    }

    // 🤖 AI Summary
    LaunchedEffect(records) {
        if (records.isNotEmpty()) {
            isAiLoading = true
            aiSummary = GeminiSummaryService.generateDoctorSummary(records)
            isAiLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Doctor Summary", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFDF8F5))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 🤖 AI MEDICAL SUMMARY
            item {
                Text(
                    "AI Medical Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isAiLoading -> {
                        CircularProgressIndicator()
                    }

                    aiSummary != null -> {
                        GeminiSummaryCard(aiSummary!!)
                    }

                    else -> {
                        Text("No summary available", color = Color.Gray)
                    }
                }
            }

            // 📄 RECENT RECORDS (VERIFICATION)
            item {
                Text(
                    "Recent Records",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            items(records.take(5)) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(record.title, fontWeight = FontWeight.Bold)
                        Text(
                            record.recordType.name.replace("_", " "),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiSummaryCard(rawText: String) {
    val sections = remember(rawText) {
        formatGeminiSummary(rawText)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            SummarySection("Patient Summary", sections.patientSummary)

            SummarySection("Active Medications", sections.medications)

            SummarySection("Important Findings", sections.findings)

            if (sections.alerts.isNotBlank()) {
                SummarySection(
                    title = "⚠ Alerts",
                    content = sections.alerts,
                    highlight = true
                )
            }
        }
    }
}

@Composable
fun SummarySection(
    title: String,
    content: String,
    highlight: Boolean = false
) {
    if (content.isBlank()) return

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = if (highlight) Color(0xFFD32F2F) else Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))

    content
        .lines()
        .filter { it.isNotBlank() }
        .forEach { line ->
            Text(
                text = "• ${line.trim()}",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
}

