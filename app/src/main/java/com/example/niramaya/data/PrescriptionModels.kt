package com.example.niramaya.data

data class PrescriptionResult(
    val doctor: String = "",
    val date: String = "",
    val diagnosis: String = "",
    val medicines: List<MedicineEntry> = emptyList(),
    val personalNotes: String = ""   // ✅ NEW
)

data class MedicineEntry(
    val name: String = "",
    val dosage: String = ""
)
