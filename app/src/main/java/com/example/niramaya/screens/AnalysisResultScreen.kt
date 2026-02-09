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
    // 🔥 RAW OCR / JSON RESULT
    val rawText = TempAnalysisStore.jsonResult ?: ""

    // 🧠 TRY PARSING AS PRESCRIPTION (SAFE)
    val parsedPrescription = remember(rawText) {
        try {
            val clean = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            parsePrescriptionJson(clean)
        } catch (e: Exception) {
            null
        }
    }

    // 🔎 DETECT RECORD TYPE
    val recordType = if (
        parsedPrescription != null &&
        parsedPrescription.medicines.isNotEmpty()
    ) {
        RecordType.PRESCRIPTION
    } else {
        RecordType.OTHER
    }

    // ✏️ STATE
    var title by remember {
        mutableStateOf(
            if (recordType == RecordType.PRESCRIPTION)
                "Prescription"
            else
                "Medical Report"
        )
    }

    var personalNotes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val medicines = remember {
        mutableStateListOf<MedicineEntry>().apply {
            parsedPrescription?.medicines?.let { addAll(it) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Extracted Data", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFDF8F5)
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    isSaving = true

                    FirestoreRepository.saveRecord(
                        recordType = recordType,
                        title = title,
                        extractedText = rawText,
                        medicines = if (recordType == RecordType.PRESCRIPTION)
                            medicines.toList()
                        else emptyList(),
                        personalNotes = personalNotes.trim(),
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
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm & Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    "Review what the AI extracted. Edit anything if needed.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Record Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { personalNotes = it },
                    label = { Text("Personal Notes (Optional)") },
                    placeholder = {
                        Text("Things you want to remember…")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))
            }

            // 💊 MEDICINES SECTION (ONLY IF PRESCRIPTION)
            if (recordType == RecordType.PRESCRIPTION) {

                item {
                    Text(
                        "Medicines Found",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F3D6E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                itemsIndexed(medicines) { index, med ->
                    EditableMedCard(
                        entry = med,
                        onNameChange = {
                            medicines[index] = medicines[index].copy(name = it)
                        },
                        onDosageChange = {
                            medicines[index] = medicines[index].copy(dosage = it)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
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
            Icon(Icons.Default.Medication, contentDescription = null)
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
