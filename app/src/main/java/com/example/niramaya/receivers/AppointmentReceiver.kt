package com.example.niramaya.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.niramaya.R

class AppointmentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val doctorName = intent.getStringExtra("doctorName") ?: "Doctor"
        val time = intent.getStringExtra("time") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notification = NotificationCompat.Builder(context, "appointment_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure you have an icon, or use a system one like android.R.drawable.ic_dialog_info
            .setContentTitle("Upcoming Appointment")
            .setContentText("You have a visit with $doctorName tomorrow at $time.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}