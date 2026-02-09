package com.example.niramaya.utils

fun formatGeminiSummary(raw: String): GeminiSummarySections {
    fun section(title: String): String =
        raw.substringAfter("**$title:**", "")
            .substringBefore("**", "")
            .trim()

    return GeminiSummarySections(
        patientSummary = section("Patient Summary"),
        medications = section("Active Medications"),
        findings = section("Important Findings"),
        alerts = section("Abnormal or Critical Indicators")
    )
}

data class GeminiSummarySections(
    val patientSummary: String,
    val medications: String,
    val findings: String,
    val alerts: String
)
