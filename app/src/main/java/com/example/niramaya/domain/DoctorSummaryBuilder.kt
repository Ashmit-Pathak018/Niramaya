package com.example.niramaya.domain

import com.example.niramaya.data.*

object DoctorSummaryBuilder {

    fun build(records: List<HistoryRecord>): DoctorSummary {

        val latestPrescription =
            records.firstOrNull { it.recordType == RecordType.PRESCRIPTION }

        val activeMedicines =
            latestPrescription?.medicines ?: emptyList()

        val recentReports =
            records
                .filter { it.recordType != RecordType.PRESCRIPTION }
                .take(3)

        return DoctorSummary(
            activeMedicines = activeMedicines,
            recentReports = recentReports,
            totalRecords = records.size
        )
    }
}
