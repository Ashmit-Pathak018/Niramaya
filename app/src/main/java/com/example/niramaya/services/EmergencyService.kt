package com.example.niramaya.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.niramaya.R
import com.example.niramaya.utils.CryptoManager // 🔥 Added Import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmergencyService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Immediately start foreground to prevent Android killing the app
        startForeground(1, createPlaceholderNotification())

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // 2. Listen to live updates (so if profile changes, notif updates instantly)
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        // 🔥 DECRYPT DATA BEFORE SHOWING
                        val blood = CryptoManager.decrypt(doc.getString("bloodGroup") ?: "N/A")
                        val allergies = CryptoManager.decrypt(doc.getString("allergies") ?: "None")

                        // "phoneNumber" is the user's phone, "emergencyContactNumber" is the SOS contact
                        val emergencyPhone = CryptoManager.decrypt(doc.getString("emergencyContactNumber") ?: "N/A")
                        val emergencyName = CryptoManager.decrypt(doc.getString("emergencyContactName") ?: "Emergency Contact")

                        updateNotification(blood, allergies, emergencyName, emergencyPhone)
                    }
                }
        }

        return START_STICKY
    }

    private fun createPlaceholderNotification(): Notification {
        val channelId = "emergency_channel"
        createChannel(channelId)
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Niramaya Active")
            .setContentText("Monitoring medical status...")
            .build()
    }

    private fun updateNotification(blood: String, allergies: String, eName: String, ePhone: String) {
        val channelId = "emergency_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ SAFE LAUNCHER INTENT
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 EMERGENCY MEDICAL ID")
            .setContentText("Blood: $blood | Allergies: $allergies") // Collapsed view
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Blood Type: $blood\n" +
                            "Allergies: $allergies\n" +
                            "SOS Contact: $eName ($ePhone)"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX) // Heads-up notification
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // SHOW ON LOCK SCREEN
            .setOngoing(true) // Cannot be swiped away
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1, notification)
    }

    private fun createChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                "Emergency Info",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows medical info on lock screen"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }
}