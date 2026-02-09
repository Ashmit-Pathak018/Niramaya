package com.example.niramaya.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiManager {

    // ⚠️ Move to local.properties later
    private const val API_KEY = "AIzaSyDah3DAOOxPiMEwn8pyz7nyV0VN0coow7M"

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f
        }
    )

    // ─────────────────────────────
    // 1️⃣ IMAGE → PRESCRIPTION JSON
    // Used by UploadScreen
    // ─────────────────────────────
    suspend fun analyzePrescription(bitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            try {
                val prompt = content {
                    image(bitmap)
                    text(
                        """
                        Extract medical information from this prescription.
                        Return ONLY valid JSON in this format:
                        {
                          "doctor": "",
                          "date": "",
                          "diagnosis": "",
                          "medicines": [
                            { "name": "", "dosage": "" }
                          ]
                        }
                        """.trimIndent()
                    )
                }

                val response = model.generateContent(prompt)
                response.text
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    // ─────────────────────────────
    // 2️⃣ TEXT → DOCTOR SUMMARY
    // Used by GeminiSummaryService
    // ─────────────────────────────
    suspend fun generateText(prompt: String): String =
        withContext(Dispatchers.IO) {
            val response = model.generateContent(prompt)
            response.text ?: ""
        }
}
