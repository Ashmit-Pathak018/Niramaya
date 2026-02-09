package com.example.niramaya.data

import org.json.JSONObject

fun parsePrescriptionJson(json: String): PrescriptionResult {
    val obj = JSONObject(json)

    val doctor = obj.optString("doctor", "")
    val date = obj.optString("date", "")
    val diagnosis = obj.optString("diagnosis", "")

    val medicinesJson = obj.optJSONArray("medicines")
    val medicines = mutableListOf<MedicineEntry>()

    if (medicinesJson != null) {
        for (i in 0 until medicinesJson.length()) {
            val medObj = medicinesJson.getJSONObject(i)
            medicines.add(
                MedicineEntry(
                    name = medObj.optString("name", ""),
                    dosage = medObj.optString("dosage", "")
                )
            )
        }
    }

    return PrescriptionResult(
        doctor = doctor,
        date = date,
        diagnosis = diagnosis,
        medicines = medicines
    )
}
