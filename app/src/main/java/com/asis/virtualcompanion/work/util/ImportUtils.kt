package com.asis.virtualcompanion.work.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

class ImportUtils(private val context: Context) {
    
    fun validateStorageSpace(requiredSpace: Long): Boolean {
        val outputDir = getImportDirectory()
        val availableSpace = outputDir.usableSpace
        return availableSpace >= requiredSpace
    }
    
    fun getImportDirectory(): File {
        val dir = File(context.filesDir, "whatsapp_import")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    fun getArchiveSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    fun isArchiveValid(uri: Uri): Boolean {
        return try {
            val size = getArchiveSize(uri)
            if (size == 0L) return false
            
            // Check if it's a valid ZIP file by reading the first few bytes
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val header = ByteArray(4)
                val bytesRead = inputStream.read(header)
                
                // ZIP files start with PK (0x504B)
                bytesRead == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    fun cleanupImportDirectory() {
        try {
            val outputDir = getImportDirectory()
            if (outputDir.exists()) {
                outputDir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        val gb = mb / 1024.0
        return "%.1f GB".format(gb)
    }
    
    fun getEstimatedProcessingTime(messageCount: Int, voiceNoteCount: Int): Long {
        // Rough estimates (in milliseconds)
        val timePerMessage = 5L // 5ms per message for parsing and saving
        val timePerVoiceNote = 500L // 500ms per voice note for processing
        val baseTime = 5000L // 5 seconds base time for setup and cleanup
        
        return (messageCount * timePerMessage) + (voiceNoteCount * timePerVoiceNote) + baseTime
    }
    
    fun createBackupBeforeImport(): Boolean {
        return try {
            val backupDir = File(context.filesDir, "backup_${System.currentTimeMillis()}")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // This is a placeholder for backup logic
            // In a real implementation, you might backup existing data
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}