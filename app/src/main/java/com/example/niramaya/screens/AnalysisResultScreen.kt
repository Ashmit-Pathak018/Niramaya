package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Medication
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
fun AnalysisResultScreen(
    navController: NavController
) {
    // --- FETCH RAW JSON FROM MEMORY ---
    val rawJson = TempAnalysisStore.jsonResult ?: ""

    // --- PARSE JSON SAFELY ---
    val parsedResult: PrescriptionResult = remember(rawJson) {
        try {
            val cleanJson = rawJson
                .replace("```json", "")
                .replace("```", "")
                .trim()

            parsePrescriptionJson(cleanJson)
        } catch (e: Exception) {
            PrescriptionResult(
                doctor = "",
                date = "",
                diagnosis = "",
                medicines = emptyList()
            )
        }
    }

    // --- EDITABLE STATE ---
    var doctorName by remember { mutableStateOf(parsedResult.doctor) }
    var visitDate by remember { mutableStateOf(parsedResult.date) }
    var isSaving by remember { mutableStateOf(false) }

    val medicines = remember {
        mutableStateListOf<MedicineEntry>().apply {
            addAll(parsedResult.medicines)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Details", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFDF8F5)
                )
            )
        },
        bottomBar = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val finalResult = PrescriptionResult(
                        doctor = doctorName,
                        date = visitDate,
                        diagnosis = parsedResult.diagnosis,
                        medicines = medicines.toList()
                    )

                    isSaving = true

                    FirestoreRepository.savePrescription(
                        prescription = finalResult,
                        onSuccess = {
                            TempAnalysisStore.jsonResult = null
                            isSaving = false
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onFailure = {
                            isSaving = false
                        }
                    )
                },
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
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Confirm & Save to Record",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                Text(
                    "Please check the info extracted by AI. Tap any field to edit.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

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

                Text(
                    "Medicines Found:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3D6E)
                )

                Spacer(modifier = Modifier.height(12.dp))
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

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun EditableMedCard(
    entry: MedicineEntry,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Medication,
                contentDescription = null,
                tint = Color(0xFF0F3D6E)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                BasicTextField(
                    value = entry.name,
                    onValueChange = onNameChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                BasicTextField(
                    value = entry.dosage,
                    onValueChange = onDosageChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}
