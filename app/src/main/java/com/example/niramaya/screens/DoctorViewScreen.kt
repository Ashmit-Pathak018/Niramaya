package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    // 🔥 REAL-TIME LISTENER
    LaunchedEffect(Unit) {
        FirestoreRepository.listenToRecords { updatedRecords ->
            records = updatedRecords
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Doctor View", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFDF8F5))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SUMMARY ---
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Patient Summary", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val latest = records.firstOrNull()
                        Text("Last Visit: ${latest?.date ?: "N/A"}")
                        Text("Doctor: ${latest?.doctor ?: "N/A"}")
                    }
                }
            }

            // --- ACTIVE MEDS ---
            item {
                Text("Active Medications", fontWeight = FontWeight.Bold)
            }

            val allMeds = records
                .flatMap { it.medicines }
                .distinctBy { it.name }

            items(allMeds) { med ->
                Text("• ${med.name} — ${med.dosage}")
            }

            // --- RECENT HISTORY ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent History", fontWeight = FontWeight.Bold)
            }

            items(records.take(3)) { record ->
                Text("• ${record.type} — ${record.date}")
            }
        }
    }
}
