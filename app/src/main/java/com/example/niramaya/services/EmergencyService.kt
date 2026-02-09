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
import com.google.firebase.firestore.FirebaseFirestore

class EmergencyService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val blood = doc.getString("bloodGroup") ?: "N/A"
                    val allergies = doc.getString("allergies") ?: "None"
                    val phone = doc.getString("phone") ?: "N/A"

                    showNotification(blood, allergies, phone)
                }
        }

        return START_STICKY // Restarts if the system kills it
    }

    private fun showNotification(blood: String, allergies: String, phone: String) {
        val channelId = "emergency_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Emergency Info",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows medical info on lock screen"
                // THIS IS THE KEY: Allows content to show without unlocking
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 EMERGENCY MEDICAL ID")
            // Short text for the collapsed view
            .setContentText("Blood: $blood | Allergies: $allergies")
            // Long text for the expanded view
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Blood Type: $blood\nAllergies: $allergies\nContact: $phone"))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Use MAX for higher visibility
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Categorize as alarm/emergency
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // <--- CRITICAL
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }
}