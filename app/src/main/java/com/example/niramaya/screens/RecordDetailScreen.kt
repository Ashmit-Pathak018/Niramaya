package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun RecordDetailScreen(
    navController: NavController,
    recordId: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // ─── RECORD STATE ───
    var title by remember { mutableStateOf("") }
    var extractedText by remember { mutableStateOf("") }
    var personalNotes by remember { mutableStateOf("") }
    var recordType by remember { mutableStateOf(RecordType.OTHER) }

    val medicines = remember { mutableStateListOf<MedicineEntry>() }

    // ─── FETCH RECORD (SAFE) ───
    LaunchedEffect(recordId) {
        FirestoreRepository.fetchRecordById(
            recordId = recordId,
            onSuccess = { record ->
                title = record.title
                extractedText = record.extractedText
                personalNotes = record.personalNotes
                recordType = record.recordType

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
            Button(
                onClick = {
                    isSaving = true

                    FirestoreRepository.updateRecord(
                        recordId = recordId,
                        title = title,
                        extractedText = extractedText,
                        medicines = if (recordType == RecordType.PRESCRIPTION)
                            medicines.toList()
                        else emptyList(),
                        personalNotes = personalNotes.trim(),
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ─── BASIC INFO ───
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Record Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    label = { Text("Extracted Text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 8,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { personalNotes = it },
                    label = { Text("Personal Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 💊 EDITABLE MEDICINES
            if (recordType == RecordType.PRESCRIPTION) {

                item {
                    Text(
                        "Medicines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F3D6E)
                    )
                }

                itemsIndexed(medicines) { index, med ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Medication, null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Medicine ${index + 1}",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = med.name,
                                onValueChange = {
                                    medicines[index] =
                                        medicines[index].copy(name = it)
                                },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = med.dosage,
                                onValueChange = {
                                    medicines[index] =
                                        medicines[index].copy(dosage = it)
                                },
                                label = { Text("Dosage") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}
