package com.asis.virtualcompanion.work.archive

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.asis.virtualcompanion.common.Constants
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ArchiveExtractor(private val context: Context) {
    
    private val bufferSize = 8192 // 8KB buffer for streaming
    
    fun extractArchive(archiveUri: Uri, outputDir: File): ArchiveExtractionResult {
        return try {
            context.contentResolver.openInputStream(archiveUri)?.use { inputStream ->
                val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
                extractZipFile(zipInputStream, outputDir)
            } ?: throw IOException("Failed to open archive file")
        } catch (e: Exception) {
            ArchiveExtractionResult(
                success = false,
                error = e.message ?: "Unknown error during extraction"
            )
        }
    }
    
    private fun extractZipFile(zipInputStream: ZipInputStream, outputDir: File): ArchiveExtractionResult {
        val extractedFiles = mutableListOf<String>()
        var chatFile: File? = null
        val mediaFiles = mutableListOf<String>()
        val voiceNotes = mutableListOf<String>()
        
        try {
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val fileName = entry.name
                    val outputFile = File(outputDir, fileName)
                    
                    // Ensure parent directories exist
                    outputFile.parentFile?.mkdirs()
                    
                    // Extract file
                    extractFile(zipInputStream, outputFile)
                    extractedFiles.add(fileName)
                    
                    // Categorize files
                    when {
                        fileName.equals("_chat.txt", ignoreCase = true) -> {
                            chatFile = outputFile
                        }
                        isMediaFile(fileName) -> {
                            mediaFiles.add(fileName)
                        }
                        isVoiceNoteFile(fileName) -> {
                            voiceNotes.add(fileName)
                        }
                    }
                }
                
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            
            return ArchiveExtractionResult(
                success = true,
                chatFile = chatFile,
                mediaFiles = mediaFiles,
                voiceNotes = voiceNotes,
                extractedFiles = extractedFiles
            )
            
        } catch (e: Exception) {
            return ArchiveExtractionResult(
                success = false,
                error = e.message ?: "Error during ZIP extraction"
            )
        }
    }
    
    private fun extractFile(zipInputStream: ZipInputStream, outputFile: File) {
        FileOutputStream(outputFile).use { fileOutputStream ->
            BufferedOutputStream(fileOutputStream, bufferSize).use { bufferedOutput ->
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int
                
                while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                    bufferedOutput.write(buffer, 0, bytesRead)
                }
                
                bufferedOutput.flush()
            }
        }
    }
    
    private fun isMediaFile(fileName: String): Boolean {
        val mediaExtensions = listOf(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", // Images
            ".mp4", ".avi", ".mov", ".mkv", ".webm", // Videos
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", // Documents
            ".vcf", // Contacts
            ".txt", ".log" // Text files
        )
        
        return mediaExtensions.any { extension ->
            fileName.lowercase().endsWith(extension)
        }
    }
    
    private fun isVoiceNoteFile(fileName: String): Boolean {
        val voiceNotePatterns = listOf(
            Regex("""PTT-\d{4}-\d{2}-\d{2}-WA\d+\.opus""", RegexOption.IGNORE_CASE),
            Regex("""voice_note_\d+\.opus""", RegexOption.IGNORE_CASE),
            Regex("""audio_\d+\.opus""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.opus""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.mp3""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.wav""", RegexOption.IGNORE_CASE),
            Regex(""".*voice.*\.m4a""", RegexOption.IGNORE_CASE)
        )
        
        return voiceNotePatterns.any { it.matches(fileName) } ||
               fileName.lowercase().contains("voice") ||
               fileName.lowercase().contains("audio") ||
               fileName.lowercase().endsWith(".opus")
    }
    
    fun getOutputDirectory(): File {
        val outputDir = File(context.filesDir, "whatsapp_import")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return outputDir
    }
    
    fun cleanupImportDirectory() {
        try {
            val outputDir = getOutputDirectory()
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
    
    fun getArchiveSize(archiveUri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(archiveUri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    fun validateArchive(archiveUri: Uri): ArchiveValidationResult {
        return try {
            val size = getArchiveSize(archiveUri)
            if (size == 0L) {
                return ArchiveValidationResult(
                    isValid = false,
                    error = "Archive file is empty or inaccessible"
                )
            }
            
            // Check if it's a valid ZIP file by reading the first few bytes
            context.contentResolver.openInputStream(archiveUri)?.use { inputStream ->
                val header = ByteArray(4)
                inputStream.read(header)
                
                // ZIP files start with PK (0x504B)
                val isValidZip = header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
                
                if (!isValidZip) {
                    return ArchiveValidationResult(
                        isValid = false,
                        error = "File is not a valid ZIP archive"
                    )
                }
                
                // Quick scan for chat file
                val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
                var hasChatFile = false
                
                try {
                    var entry: ZipEntry? = zipInputStream.nextEntry
                    while (entry != null && !hasChatFile) {
                        if (!entry.isDirectory && entry.name.equals("_chat.txt", ignoreCase = true)) {
                            hasChatFile = true
                        }
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                } catch (e: Exception) {
                    // Ignore scanning errors
                }
                
                if (!hasChatFile) {
                    return ArchiveValidationResult(
                        isValid = false,
                        error = "Archive does not contain a _chat.txt file"
                    )
                }
                
                ArchiveValidationResult(isValid = true)
            } ?: ArchiveValidationResult(
                isValid = false,
                error = "Could not read archive file"
            )
            
        } catch (e: Exception) {
            ArchiveValidationResult(
                isValid = false,
                error = "Error validating archive: ${e.message}"
            )
        }
    }
}

data class ArchiveExtractionResult(
    val success: Boolean,
    val chatFile: File? = null,
    val mediaFiles: List<String> = emptyList(),
    val voiceNotes: List<String> = emptyList(),
    val extractedFiles: List<String> = emptyList(),
    val error: String? = null
)

data class ArchiveValidationResult(
    val isValid: Boolean,
    val error: String? = null
)