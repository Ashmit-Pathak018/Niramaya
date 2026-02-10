package com.example.niramaya.data

import com.example.niramaya.utils.CryptoManager
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
            "title" to CryptoManager.encrypt(title),
            "extractedText" to CryptoManager.encrypt(extractedText),
            "personalNotes" to CryptoManager.encrypt(personalNotes),
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
    // LISTEN TO RECORDS
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
            "title" to CryptoManager.encrypt(title),
            "extractedText" to CryptoManager.encrypt(extractedText),
            "personalNotes" to CryptoManager.encrypt(personalNotes),
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
    //  🔥 USER PROFILE (ENCRYPTED) - FIXED!
    // ─────────────────────────────────────────────
    fun saveUserProfile(
        fullName: String,
        phoneNumber: String,
        bloodGroup: String,
        age: String,
        gender: String,
        emergencyName: String, // Added
        emergencyPhone: String, // Added
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userMap = hashMapOf(
            "fullName" to CryptoManager.encrypt(fullName),
            "phoneNumber" to CryptoManager.encrypt(phoneNumber),
            "bloodGroup" to CryptoManager.encrypt(bloodGroup),
            "age" to CryptoManager.encrypt(age),
            "gender" to CryptoManager.encrypt(gender),
            // 🔥 NOW SAVING EMERGENCY INFO HERE TOO
            "emergencyContactName" to CryptoManager.encrypt(emergencyName),
            "emergencyContactNumber" to CryptoManager.encrypt(emergencyPhone)
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update(userMap as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
    }

    fun getUserProfile(
        onSuccess: (Map<String, String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileData = mapOf(
                        "fullName" to CryptoManager.decrypt(document.getString("fullName") ?: ""),
                        "phoneNumber" to CryptoManager.decrypt(document.getString("phoneNumber") ?: ""),
                        "bloodGroup" to CryptoManager.decrypt(document.getString("bloodGroup") ?: ""),
                        "age" to CryptoManager.decrypt(document.getString("age") ?: ""),
                        "gender" to CryptoManager.decrypt(document.getString("gender") ?: ""),

                        // 🔥 THIS WAS MISSING! NOW IT FETCHES THE CONTACTS!
                        "emergencyContactName" to CryptoManager.decrypt(document.getString("emergencyContactName") ?: ""),
                        "emergencyContactNumber" to CryptoManager.decrypt(document.getString("emergencyContactNumber") ?: ""),
                        "disease" to CryptoManager.decrypt(document.getString("disease") ?: ""),
                        "allergies" to CryptoManager.decrypt(document.getString("allergies") ?: ""),

                        "email" to (document.getString("email") ?: ""),
                        "profilePic" to (document.getString("profilePic") ?: "")
                    )
                    onSuccess(profileData)
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // ─────────────────────────────────────────────
    // INTERNAL MAPPER
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
            title = CryptoManager.decrypt(doc.getString("title") ?: "Medical Record"),
            recordType = RecordType.valueOf(
                doc.getString("recordType") ?: "OTHER"
            ),
            createdAt = createdAtMillis,
            extractedText = CryptoManager.decrypt(doc.getString("extractedText") ?: ""),
            medicines = medicines,
            personalNotes = CryptoManager.decrypt(doc.getString("personalNotes") ?: "")
        )
    }
}