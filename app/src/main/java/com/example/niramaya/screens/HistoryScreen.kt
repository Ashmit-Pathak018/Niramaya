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

@Composable
fun HistoryScreen(navController: NavController) {

    var records by remember { mutableStateOf<List<HistoryRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var recordToDelete by remember { mutableStateOf<HistoryRecord?>(null) }

    // 🔥 LIVE HISTORY (NO CHANGE)
    LaunchedEffect(Unit) {
        FirestoreRepository.listenToRecords {
            records = it
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
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

        // ─── SEARCH (UI ONLY) ───
        OutlinedTextField(
            value = "",
            onValueChange = {},
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
                CircularProgressIndicator(color = Color(0xFF0F3D6E))
            }

            records.isEmpty() -> {
                Text(
                    "No medical records found",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(records) { record ->
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

    // ─── DELETE CONFIRMATION DIALOG ───
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = {
                Text("Delete Record?")
            },
            text = {
                Text("This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirestoreRepository.deleteRecord(
                            recordId = record.id,
                            onSuccess = {
                                recordToDelete = null
                            },
                            onFailure = { exception ->
                                exception.printStackTrace()
                                recordToDelete = null
                            }
                        )
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { recordToDelete = null }
                ) {
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
                .padding(16.dp)
                .clickable { onOpen() },
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
                        .ifBlank { "No extracted text" } + "…",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // 🗑 DELETE BUTTON
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}
