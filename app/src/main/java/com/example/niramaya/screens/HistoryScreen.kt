package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
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
fun HistoryScreen(navController: NavController) {

    var records by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var recordToDelete by remember { mutableStateOf<HistoryRecord?>(null) }
    var searchQuery by remember { mutableStateOf("") } // Added state for search

    // 🔥 LIVE HISTORY
    LaunchedEffect(Unit) {
        FirestoreRepository.listenToRecords {
            records = it
            isLoading = false
        }
    }

    // Filter records based on search
    val filteredRecords = records.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.extractedText.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = Color(0xFFFDF8F5),
        floatingActionButton = {
            // 🔥 THIS IS THE BUTTON YOU WERE MISSING
            ExtendedFloatingActionButton(
                onClick = {
                    // ✅ CORRECT ROUTE: Points to AI Summary
                    navController.navigate("doctor_view")
                },
                containerColor = Color(0xFF0F3D6E),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AutoAwesome, "AI Summary") },
                text = { Text("Doctor Summary") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {

            // ─── HEADER ───
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F3D6E),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Medical History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3D6E)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── SEARCH ───
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search records...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF0F3D6E))
                    }
                }

                records.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No medical records found", color = Color.Gray)
                    }
                }

                filteredRecords.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching records", color = Color.Gray)
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                    ) {
                        items(filteredRecords) { record ->
                            HistoryItem(
                                record = record,
                                onOpen = {
                                    navController.navigate("record_detail/${record.id}")
                                },
                                onDelete = {
                                    recordToDelete = record
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ─── DELETE CONFIRMATION DIALOG ───
    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Record?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete?.let { record ->
                            FirestoreRepository.deleteRecord(
                                recordId = record.id,
                                onSuccess = { recordToDelete = null },
                                onFailure = { recordToDelete = null }
                            )
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryItem(
    record: HistoryRecord,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onOpen() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ─── ICON ───
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFEAF2F8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF0F3D6E)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ─── CONTENT ───
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.recordType.name.replace("_", " "),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = record.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = record.extractedText
                        .take(60)
                        .replace("\n", " ")
                        .ifBlank { "No extracted text" } + "…",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }

            // 🗑 DELETE BUTTON
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}