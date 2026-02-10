package com.example.niramaya.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.niramaya.MainActivity
import com.example.niramaya.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class PushNotificationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            listenForNewNotifications(userId)
        }
        return START_STICKY
    }

    private fun listenForNewNotifications(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // We track the time the service started so we don't alert for OLD notifications
        val startTime = System.currentTimeMillis()

        db.collection("users").document(userId)
            .collection("notifications")
            .whereGreaterThan("timestamp", startTime) // 🔥 ONLY FUTURE NOTIFICATIONS
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {

                        // 🔥 CHECK PREFERENCES BEFORE SENDING
                        db.collection("users").document(userId)
                            .collection("settings").document("preferences")
                            .get()
                            .addOnSuccessListener { prefs ->
                                // Default to TRUE (enabled) if the setting doesn't exist yet
                                val isEnabled = prefs.getBoolean("pushNotifications") ?: true

                                if (isEnabled) {
                                    val title = dc.document.getString("title") ?: "New Alert"
                                    val message = dc.document.getString("message") ?: "You have a new notification."

                                    // If you enabled encryption for notifs earlier, uncomment this:
                                    // val decryptedMsg = com.example.niramaya.utils.CryptoManager.decrypt(message)

                                    sendPushNotification(title, message)
                                }
                            }
                    }
                }
            }
    }

    private fun sendPushNotification(title: String, message: String) {
        val channelId = "niramaya_alerts"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create Channel (Sound & Pop-up)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders & Alerts",
                NotificationManager.IMPORTANCE_HIGH // Makes a sound!
            ).apply {
                description = "Medicine and Doctor Reminders"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // 2. Intent to open App when clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build & Show
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Disappears when clicked
            .setContentIntent(pendingIntent)
            .build()

        // Use a random ID so multiple notifications stack instead of replacing each other
        val notificationId = System.currentTimeMillis().toInt()
        manager.notify(notificationId, notification)
    }
}