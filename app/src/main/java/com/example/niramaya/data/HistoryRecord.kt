package com.example.niramaya.data

data class HistoryRecord(
    val id: String = "",
    val title: String = "",
    val recordType: RecordType = RecordType.OTHER,
    val createdAt: Long = 0L,
    val extractedText: String = "",
    val medicines: List<MedicineEntry> = emptyList(),
    val personalNotes: String = ""
)

enum class RecordType {
    PRESCRIPTION,
    BLOOD_REPORT,
    SCAN,
    OTHER
}
