package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notes
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorViewScreen(navController: NavController) {

    var records by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 LIVE RECORD STREAM
    LaunchedEffect(Unit) {
        FirestoreRepository.listenToRecords { updatedRecords ->
            records = updatedRecords.sortedByDescending { it.date }
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Doctor View", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            records.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("No medical records yet", color = Color.Gray)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFFDF8F5))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(records) { record ->
                        DoctorRecordCard(record)
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorRecordCard(record: HistoryRecord) {

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Text(
                text = record.date,
                fontSize = 13.sp,
                color = Color.Gray
            )

            Text(
                text = "Dr. ${record.doctor}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Medicines
            Row {
                Icon(Icons.Default.Medication, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Medicines", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(6.dp))

            record.medicines.forEach { med ->
                Text(
                    "• ${med.name} — ${med.dosage}",
                    fontSize = 14.sp
                )
            }

            // Patient Notes (if any)
            if (record.personalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    Icon(Icons.Default.Notes, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Patient Notes", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = record.personalNotes,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

