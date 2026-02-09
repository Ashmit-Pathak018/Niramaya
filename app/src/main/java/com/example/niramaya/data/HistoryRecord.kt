package com.example.niramaya.data

data class HistoryRecord(
    val id: String = "",
    val doctor: String = "",
    val date: String = "",
    val type: String = "Prescription", // ✅ ADD THIS
    val medicines: List<MedicineEntry> = emptyList(),
    val personalNotes: String = ""
)
