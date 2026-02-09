package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    navController: NavController,
    recordId: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var doctorName by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }

    val medicines = remember { mutableStateListOf<MedicineEntry>() }

    // --- FETCH RECORD ---
    LaunchedEffect(recordId) {
        FirestoreRepository.fetchRecordById(
            recordId = recordId,
            onSuccess = { record ->
                doctorName = record.doctor
                visitDate = record.date
                medicines.clear()
                medicines.addAll(record.medicines)
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Record", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFDF8F5)
                )
            )
        },
        bottomBar = {
            Column {
                // 🗑 DELETE BUTTON
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Delete Record",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 💾 SAVE BUTTON
                Button(
                    onClick = {
                        isSaving = true
                        FirestoreRepository.updatePrescription(
                            recordId = recordId,
                            prescription = PrescriptionResult(
                                doctor = doctorName,
                                date = visitDate,
                                diagnosis = "",
                                medicines = medicines.toList()
                            ),
                            onSuccess = {
                                isSaving = false
                                navController.popBackStack()
                            },
                            onFailure = {
                                isSaving = false
                            }
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFDF8F5))
                .padding(horizontal = 24.dp)
        ) {

            item {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = visitDate,
                    onValueChange = { visitDate = it },
                    label = { Text("Date of Visit") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            itemsIndexed(medicines) { index, med ->
                EditableMedCard(
                    entry = med,
                    onNameChange = {
                        medicines[index] =
                            medicines[index].copy(name = it)
                    },
                    onDosageChange = {
                        medicines[index] =
                            medicines[index].copy(dosage = it)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // ⚠️ DELETE CONFIRMATION DIALOG
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Delete Record?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        FirestoreRepository.deleteRecord(
                            recordId = recordId,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onFailure = {}
                        )
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
