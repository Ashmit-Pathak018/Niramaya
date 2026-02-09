package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class SelectableMed(
    val id: String,
    val name: String,
    val dosage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectMedicinesScreen(navController: NavController) {

    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()

    var meds by remember { mutableStateOf<List<SelectableMed>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isSaving by remember { mutableStateOf(false) }

    // 🔥 LOAD ACTIVE + ALL MEDS (NO await)
    LaunchedEffect(Unit) {

        // 1️⃣ Load active meds first
        db.collection("users")
            .document(user.uid)
            .collection("active_medications")
            .get()
            .addOnSuccessListener { activeSnap ->

                selectedIds = activeSnap.documents.mapNotNull {
                    val name = it.getString("name")
                    val dosage = it.getString("dosage")
                    if (name != null && dosage != null) "$name|$dosage" else null
                }.toSet()

                // 2️⃣ Load all prescription meds
                db.collection("users")
                    .document(user.uid)
                    .collection("records")
                    .get()
                    .addOnSuccessListener { recordSnap ->

                        val allMeds = mutableListOf<SelectableMed>()

                        recordSnap.documents.forEach { doc ->
                            val medicines =
                                doc.get("medicines") as? List<Map<String, String>>

                            medicines?.forEach {
                                val name = it["name"]
                                val dosage = it["dosage"]
                                if (name != null && dosage != null) {
                                    allMeds.add(
                                        SelectableMed(
                                            id = "$name|$dosage",
                                            name = name,
                                            dosage = dosage
                                        )
                                    )
                                }
                            }
                        }

                        meds = allMeds.distinctBy { it.id }
                    }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Select Active Medicines",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },

        bottomBar = {
            Button(
                onClick = {
                    isSaving = true

                    val activeRef = db.collection("users")
                        .document(user.uid)
                        .collection("active_medications")

                    // 🔥 Clear old
                    activeRef.get()
                        .addOnSuccessListener { snap ->
                            snap.documents.forEach { it.reference.delete() }

                            // 🔥 Save selected
                            selectedIds.forEach { id ->
                                val parts = id.split("|")
                                activeRef.add(
                                    mapOf(
                                        "name" to parts[0],
                                        "dosage" to parts[1]
                                    )
                                )
                            }

                            isSaving = false
                            navController.popBackStack()
                        }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(55.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Active Medications", fontSize = 16.sp)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(meds) { med ->

                val isSelected = selectedIds.contains(med.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedIds =
                                if (isSelected)
                                    selectedIds - med.id
                                else
                                    selectedIds + med.id
                        },
                    colors = CardDefaults.cardColors(
                        containerColor =
                            if (isSelected) Color(0xFFE3F2FD)
                            else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Medication, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(med.name, fontWeight = FontWeight.Bold)
                            Text(
                                med.dosage,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
