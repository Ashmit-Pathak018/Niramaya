package com.example.niramaya.domain

import com.example.niramaya.data.*

data class DoctorSummary(
    val activeMedicines: List<MedicineEntry>,
    val recentReports: List<HistoryRecord>,
    val totalRecords: Int
)
