package com.example.niramaya.data

data class Appointment(
    val id: String = "",
    val doctorName: String = "",
    val purpose: String = "General Checkup",
    val timestamp: Long = 0L, // For sorting
    val dateStr: String = "",
    val timeStr: String = ""
)