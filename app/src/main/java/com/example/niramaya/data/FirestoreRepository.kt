package com.example.niramaya.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

object FirestoreRepository {

    // -----------------------------
    // SAVE NEW PRESCRIPTION
    // -----------------------------
    fun savePrescription(
        prescription: PrescriptionResult,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return onFailure(Exception("User not logged in"))

        val record = hashMapOf(
            "type" to "Prescription",
            "doctor" to prescription.doctor,
            "date" to prescription.date,
            "diagnosis" to prescription.diagnosis,
            "medicines" to prescription.medicines.map {
                mapOf(
                    "name" to it.name,
                    "dosage" to it.dosage
                )
            },
            "personalNotes" to "",
            "createdAt" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("records")
            .add(record)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // -----------------------------
    // FETCH HISTORY (ONE-TIME)
    // -----------------------------
    fun fetchHistory(
        onSuccess: (List<HistoryRecord>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return onFailure(Exception("User not logged in"))

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("records")
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { snapshot ->
                val records = snapshot.documents.mapNotNull { doc ->
                    val medicines =
                        (doc.get("medicines") as? List<Map<String, String>>)
                            ?.map {
                                MedicineEntry(
                                    name = it["name"] ?: "",
                                    dosage = it["dosage"] ?: ""
                                )
                            } ?: emptyList()

                    HistoryRecord(
                        id = doc.id,
                        doctor = doc.getString("doctor") ?: "",
                        date = doc.getString("date") ?: "",
                        type = doc.getString("type") ?: "Prescription",
                        medicines = medicines,
                        personalNotes = doc.getString("personalNotes") ?: ""
                    )
                }.reversed()

                onSuccess(records)
            }
            .addOnFailureListener { onFailure(it) }
    }

    // -----------------------------
    // LIVE LISTENER (DOCTOR VIEW)
    // -----------------------------
    fun listenToRecords(
        onUpdate: (List<HistoryRecord>) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("records")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val records = snapshot.documents.mapNotNull { doc ->
                    val medicines =
                        (doc.get("medicines") as? List<Map<String, String>>)
                            ?.map {
                                MedicineEntry(
                                    name = it["name"] ?: "",
                                    dosage = it["dosage"] ?: ""
                                )
                            } ?: emptyList()

                    HistoryRecord(
                        id = doc.id,
                        doctor = doc.getString("doctor") ?: "",
                        date = doc.getString("date") ?: "",
                        type = doc.getString("type") ?: "Prescription",
                        medicines = medicines,
                        personalNotes = doc.getString("personalNotes") ?: ""
                    )
                }.reversed()

                onUpdate(records)
            }
    }

    // -----------------------------
    // FETCH SINGLE RECORD
    // -----------------------------
    fun fetchRecordById(
        recordId: String,
        onSuccess: (PrescriptionResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return onFailure(Exception("Not logged in"))

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("records")
            .document(recordId)
            .get()
            .addOnSuccessListener { doc ->
                val medicines =
                    (doc.get("medicines") as? List<Map<String, String>>)
                        ?.map {
                            MedicineEntry(
                                name = it["name"] ?: "",
                                dosage = it["dosage"] ?: ""
                            )
                        } ?: emptyList()

                onSuccess(
                    PrescriptionResult(
                        doctor = doc.getString("doctor") ?: "",
                        date = doc.getString("date") ?: "",
                        diagnosis = doc.getString("diagnosis") ?: "",
                        medicines = medicines
                    )
                )
            }
            .addOnFailureListener { onFailure(it) }
    }

    // -----------------------------
    // UPDATE EXISTING RECORD
    // -----------------------------
    fun updatePrescription(
        recordId: String,
        prescription: PrescriptionResult,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return onFailure(Exception("User not logged in"))

        val updatedData = hashMapOf(
            "doctor" to prescription.doctor,
            "date" to prescription.date,
            "diagnosis" to prescription.diagnosis,
            "medicines" to prescription.medicines.map {
                mapOf(
                    "name" to it.name,
                    "dosage" to it.dosage
                )
            }
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("records")
            .document(recordId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // -----------------------------
    // DELETE RECORD
    // -----------------------------
    fun deleteRecord(
        recordId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("records")
            .document(recordId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }
}
