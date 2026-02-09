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

    var doctorName by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var personalNotes by remember { mutableStateOf("") }

    val medicines = remember { mutableStateListOf<MedicineEntry>() }

    // 🔥 Fetch record
    LaunchedEffect(recordId) {
        FirestoreRepository.fetchRecordById(
            recordId = recordId,
            onSuccess = { record ->
                doctorName = record.doctor
                visitDate = record.date
                personalNotes = record.personalNotes
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
                title = { Text("Record Details", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFDF8F5)
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    isSaving = true

                    FirestoreRepository.updatePrescription(
                        recordId = recordId,
                        prescription = PrescriptionResult(
                            doctor = doctorName,
                            date = visitDate,
                            diagnosis = "",
                            medicines = medicines.toList(),
                            personalNotes = personalNotes.trim()
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
                    .padding(24.dp)
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
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { personalNotes = it },
                    label = { Text("Personal Notes") },
                    placeholder = {
                        Text("What the doctor said but didn’t write…")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Medicines",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3D6E)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // ✅ REUSE EXISTING EditableMedCard
            itemsIndexed(medicines) { index, med ->
                EditableMedCard(
                    entry = med,
                    onNameChange = { newName ->
                        medicines[index] = medicines[index].copy(name = newName)
                    },
                    onDosageChange = { newDosage ->
                        medicines[index] = medicines[index].copy(dosage = newDosage)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
