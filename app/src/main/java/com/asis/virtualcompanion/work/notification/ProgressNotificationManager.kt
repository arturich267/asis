package com.asis.virtualcompanion.work.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.asis.virtualcompanion.common.Constants
import com.asis.virtualcompanion.ui.MainActivity

class ProgressNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = Constants.WHATSAPP_IMPORT_CHANNEL_ID
        private const val CHANNEL_NAME = "WhatsApp Import"
        private const val NOTIFICATION_ID = 1001
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for WhatsApp import progress"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    fun startImportNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Importing WhatsApp Archive")
            .setContentText("Starting import...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun updateProgress(
        currentStep: String,
        progress: Int,
        maxProgress: Int = 100,
        details: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = if (details != null) {
            "$currentStep - $details"
        } else {
            currentStep
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Importing WhatsApp Archive")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(maxProgress, progress, false)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun showIndeterminateProgress(message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Importing WhatsApp Archive")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun completeImport(messageCount: Int, voiceNoteCount: Int, phraseCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = "Imported $messageCount messages, $voiceNoteCount voice notes, and $phraseCount phrases"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("WhatsApp Import Complete")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun showErrorNotification(error: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("WhatsApp Import Failed")
            .setContentText("Error: $error")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Error: $error"))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}