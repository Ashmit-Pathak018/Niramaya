package com.example.niramaya.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiManager {
    // 1. Double-check your API Key from AI Studio
    private val apiKey = "AIzaSyBxGot5q35Q6VYQdvdsQCLJbU-_dJXMIxA"

    // 2. Try adding "models/" prefix explicitly
    // Inside GeminiManager.kt
    private val generativeModel = GenerativeModel(
        // CHANGED: gemini-1.5-flash is shut down; use 2.5-flash for 2026
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.1f
            responseMimeType = "application/json"
        }
    )

    suspend fun analyzePrescription(image: Bitmap): String? = withContext(Dispatchers.IO) {
        val prompt = content {
            image(image)
            text("""
                Extract details from this prescription into JSON.
                { "doctor": "", "date": "", "diagnosis": "", "medicines": [{ "name": "", "dosage": "" }] }
                Return ONLY the raw JSON.
            """.trimIndent())
        }

        return@withContext try {
            val response = generativeModel.generateContent(prompt)
            println("GEMINI_SUCCESS: ${response.text}")
            response.text
        } catch (e: Exception) {
            // This prints the actual error message to your Logcat
            println("GEMINI_ERROR: ${e.message}")
            null
        }
    }
}