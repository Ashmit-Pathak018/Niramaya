package com.example.niramaya.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

object FirestoreRepository {

    private fun userRecordsRef() =
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(
                FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("User not logged in")
            )
            .collection("records")

    // ─────────────────────────────────────────────
    // SAVE RECORD (UNIVERSAL)
    // ─────────────────────────────────────────────
    fun saveRecord(
        recordType: RecordType,
        title: String,
        extractedText: String,
        medicines: List<MedicineEntry>,
        personalNotes: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "recordType" to recordType.name,
            "title" to title,
            "extractedText" to extractedText,
            "personalNotes" to personalNotes,
            "medicines" to medicines.map {
                mapOf("name" to it.name, "dosage" to it.dosage)
            },
            "createdAt" to Timestamp.now()
        )

        userRecordsRef()
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // ─────────────────────────────────────────────
    // LISTEN TO RECORDS (HISTORY / DOCTOR VIEW)
    // ─────────────────────────────────────────────
    fun listenToRecords(
        onUpdate: (List<HistoryRecord>) -> Unit
    ) {
        userRecordsRef()
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val records = snapshot.documents.map { doc ->
                    mapDocToHistoryRecord(doc)
                }.reversed()

                onUpdate(records)
            }
    }

    // ─────────────────────────────────────────────
    // FETCH SINGLE RECORD
    // ─────────────────────────────────────────────
    fun fetchRecordById(
        recordId: String,
        onSuccess: (HistoryRecord) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRecordsRef()
            .document(recordId)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(mapDocToHistoryRecord(doc))
            }
            .addOnFailureListener { onFailure(it) }
    }

    // ─────────────────────────────────────────────
    // UPDATE RECORD
    // ─────────────────────────────────────────────
    fun updateRecord(
        recordId: String,
        title: String,
        extractedText: String,
        medicines: List<MedicineEntry>,
        personalNotes: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val update = hashMapOf(
            "title" to title,
            "extractedText" to extractedText,
            "personalNotes" to personalNotes,
            "medicines" to medicines.map {
                mapOf("name" to it.name, "dosage" to it.dosage)
            }
        )

        userRecordsRef()
            .document(recordId)
            .update(update)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // ─────────────────────────────────────────────
    // DELETE RECORD
    // ─────────────────────────────────────────────
    fun deleteRecord(
        recordId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRecordsRef()
            .document(recordId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // ─────────────────────────────────────────────
    // INTERNAL MAPPER (SAFE)
    // ─────────────────────────────────────────────
    private fun mapDocToHistoryRecord(
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): HistoryRecord {

        val medicines =
            (doc.get("medicines") as? List<Map<String, String>>)
                ?.map {
                    MedicineEntry(
                        name = it["name"] ?: "",
                        dosage = it["dosage"] ?: ""
                    )
                } ?: emptyList()

        val createdAtMillis =
            (doc.get("createdAt") as? Timestamp)?.toDate()?.time ?: 0L

        return HistoryRecord(
            id = doc.id,
            title = doc.getString("title") ?: "Medical Record",
            recordType = RecordType.valueOf(
                doc.getString("recordType") ?: "OTHER"
            ),
            createdAt = createdAtMillis,
            extractedText = doc.getString("extractedText") ?: "",
            medicines = medicines,
            personalNotes = doc.getString("personalNotes") ?: ""
        )
    }
}
