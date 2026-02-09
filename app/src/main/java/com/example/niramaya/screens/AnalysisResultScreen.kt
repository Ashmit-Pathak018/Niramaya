package com.example.niramaya.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun AnalysisResultScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
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
                text = "Analysis Report",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F3D6E)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SUCCESS BANNER ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Light Green
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Prescription Analyzed Successfully",
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- EXTRACTED MEDICINES ---
        Text(
            text = "Identified Medicines",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F3D6E)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Medicine 1
        MedicineCard(name = "Amoxicillin", dosage = "500mg", freq = "Twice a day", color = Color(0xFFFFCDD2))
        // Medicine 2
        MedicineCard(name = "Paracetamol", dosage = "650mg", freq = "When needed", color = Color(0xFFBBDEFB))
        // Medicine 3
        MedicineCard(name = "Cetirizine", dosage = "10mg", freq = "Nightly", color = Color(0xFFC8E6C9))

        Spacer(modifier = Modifier.height(32.dp))

        // --- DOCTOR DETAILS ---
        Text(
            text = "Doctor Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F3D6E)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Doctor:", "Dr. Ashmit Pathak")
                Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("Date:", "10th Feb 2026")
                Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("Diagnosis:", "Viral Fever & Fatigue")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- SAVE BUTTON ---
        Button(
            onClick = {
                // In real app: Save to Firestore
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D6E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save to Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MedicineCard(name: String, dosage: String, freq: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = Color.Black.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = "$dosage • $freq", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color(0xFF0F3D6E), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}