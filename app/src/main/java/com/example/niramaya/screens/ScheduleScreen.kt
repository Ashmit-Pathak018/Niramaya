package com.example.niramaya.screens

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.data.Appointment
import com.example.niramaya.receivers.AppointmentReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: return

    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // --- FIRESTORE LISTENER ---
    DisposableEffect(Unit) {
        val listener = db.collection("users").document(userId)
            .collection("appointments")
            .orderBy("timestamp") // Show nearest first
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    appointments = snap.documents.mapNotNull { doc ->
                        doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                    }
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        containerColor = Color(0xFFFDF8F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Schedule", fontWeight = FontWeight.Bold, color = Color(0xFF0F3D6E)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF0F3D6E))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFFDF8F5))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0F3D6E),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add Appointment")
            }
        }
    ) { padding ->

        if (appointments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No upcoming appointments", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(appointments) { apt ->
                    AppointmentCard(apt) {
                        // Delete logic
                        db.collection("users").document(userId)
                            .collection("appointments").document(apt.id).delete()
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- ADD DIALOG WITH NOTIFICATION LOGIC ---
    if (showAddDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { doctor, purpose, date, time, timestamp ->

                // 1. Save to Firestore
                val newApt = Appointment(
                    doctorName = doctor,
                    purpose = purpose,
                    dateStr = date,
                    timeStr = time,
                    timestamp = timestamp
                )
                db.collection("users").document(userId)
                    .collection("appointments").add(newApt)

                // 2. SCHEDULE NOTIFICATION (24 Hours Before)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, AppointmentReceiver::class.java).apply {
                    putExtra("doctorName", doctor)
                    putExtra("time", time)
                }

                // Unique ID based on timestamp (truncated to Int)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (timestamp / 1000).toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                // Calculate time: 24 hours before the timestamp
                val triggerTime = timestamp - (24 * 60 * 60 * 1000)

                // Only schedule if the reminder time is in the future
                if (triggerTime > System.currentTimeMillis()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Toast.makeText(context, "Reminder set for 24h before!", Toast.LENGTH_SHORT).show()
                    } catch (e: SecurityException) {
                        // Fallback if permission issues
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }

                showAddDialog = false
            }
        )
    }
}

@Composable
fun AppointmentCard(apt: Appointment, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color(0xFFEAF2F8), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(apt.dateStr.split(" ").firstOrNull() ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F3D6E))
                Text(apt.dateStr.split(" ").drop(1).joinToString(" "), fontSize = 12.sp, color = Color(0xFF0F3D6E))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(apt.doctorName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(apt.purpose, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("⏰ ${apt.timeStr}", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var doctorName by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var finalTimestamp by remember { mutableStateOf(0L) }

    // Date Picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    // Time Picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            selectedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
            finalTimestamp = calendar.timeInMillis
        },
        12, 0, false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Appointment") },
        text = {
            Column {
                OutlinedTextField(value = doctorName, onValueChange = { doctorName = it }, label = { Text("Doctor Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose (e.g. Checkup)") })
                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { datePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                        Text(if (selectedDate.isEmpty()) "Set Date" else selectedDate)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { timePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                        Text(if (selectedTime.isEmpty()) "Set Time" else selectedTime)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (doctorName.isNotEmpty() && selectedDate.isNotEmpty()) {
                        onConfirm(doctorName, purpose, selectedDate, selectedTime, finalTimestamp)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}