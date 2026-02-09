package com.example.niramaya.summary

import com.example.niramaya.data.GeminiManager
import com.example.niramaya.data.HistoryRecord

/**
 * Converts medical history into an AI-generated doctor-friendly summary
 */
object GeminiSummaryService {

    suspend fun generateDoctorSummary(
        records: List<HistoryRecord>
    ): String {

        if (records.isEmpty()) {
            return "No medical records available."
        }

        val compactHistory = records.joinToString("\n\n") { record ->
            """
            Type: ${record.recordType}
            Title: ${record.title}
            Notes: ${record.personalNotes}
            Extracted Text: ${record.extractedText.take(400)}
            """.trimIndent()
        }

        val prompt = """
            You are a medical AI assistant.

            Summarize the following patient history for a doctor.
            Focus on:
            - Active medications
            - Important findings
            - Any abnormal or critical indicators

            Keep it concise, structured, and professional.

            Patient History:
            $compactHistory
        """.trimIndent()

        return GeminiManager.generateText(prompt)
    }
}
