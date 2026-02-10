package com.example.niramaya.summary

import com.example.niramaya.data.HistoryRecord
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class GeminiSummaryService {

    // ⚠️ YOUR API KEY
    private val apiKey = "AIzaSyDMQKsUFyGgOpP3A8tQ9OxkLwl002lP9qk"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.3f
        }
    )

    suspend fun generateDoctorSummary(records: List<HistoryRecord>): String = withContext(Dispatchers.IO) {
        if (records.isEmpty()) return@withContext "No records available."

        val sortedRecords = records.sortedBy { it.createdAt }
        val promptBuilder = StringBuilder()

        promptBuilder.append("Analyze this patient history and generate a structured clinical summary.\n\n")
        promptBuilder.append("--- PATIENT RECORDS ---\n")

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        sortedRecords.forEachIndexed { index, record ->
            promptBuilder.append("\nRecord ${index + 1} (${dateFormat.format(record.createdAt)}): ${record.title}\n")
            if (record.medicines.isNotEmpty()) {
                val meds = record.medicines.joinToString { "${it.name} ${it.dosage}" }
                promptBuilder.append("Meds: $meds\n")
            }
            if (record.extractedText.isNotBlank()) {
                promptBuilder.append("Notes: ${record.extractedText}\n")
            }
            promptBuilder.append("---\n")
        }

        // 🔥 ROBUST PROMPT: Use # Hashtags for headers
        promptBuilder.append("\nGenerate the response using EXACTLY these 4 headers:\n")
        promptBuilder.append("# Patient Summary\n")
        promptBuilder.append("# Active Medications\n")
        promptBuilder.append("# Important Findings\n")
        promptBuilder.append("# Alerts\n")

        try {
            val response = generativeModel.generateContent(promptBuilder.toString())
            response.text ?: "AI returned empty response."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}