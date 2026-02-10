package com.example.niramaya.utils

data class GeminiSummarySections(
    val patientSummary: String = "",
    val medications: String = "",
    val findings: String = "",
    val alerts: String = ""
)

fun formatGeminiSummary(raw: String): GeminiSummarySections {
    // 1. Clean up the text
    val cleanText = raw.replace("**", "").replace("##", "#")

    // 2. Robust Extraction Function
    fun extract(header: String): String {
        if (!cleanText.contains(header, ignoreCase = true)) return ""

        // Get everything after the header
        val afterHeader = cleanText.substringAfter(header)

        // Find the start of the NEXT header (any line starting with #)
        val nextHeaderIndex = afterHeader.indexOf("\n#")

        // Return text up to the next header, or the end of string
        return if (nextHeaderIndex != -1) {
            afterHeader.substring(0, nextHeaderIndex).trim()
        } else {
            afterHeader.trim()
        }
    }

    return GeminiSummarySections(
        patientSummary = extract("# Patient Summary"),
        medications = extract("# Active Medications"),
        findings = extract("# Important Findings"),
        alerts = extract("# Alerts")
    )
}