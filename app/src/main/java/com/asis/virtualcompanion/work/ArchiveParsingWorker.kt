package com.asis.virtualcompanion.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.asis.virtualcompanion.AsisApplication
import com.asis.virtualcompanion.data.database.entity.MessageEntity
import com.asis.virtualcompanion.data.database.entity.PhraseStatEntity
import com.asis.virtualcompanion.data.database.entity.VoiceMetaEntity
import com.asis.virtualcompanion.di.AppModule
import com.asis.virtualcompanion.work.analytics.NLPAnalyticsProcessor
import com.asis.virtualcompanion.work.archive.ArchiveExtractor
import com.asis.virtualcompanion.work.notification.ProgressNotificationManager
import com.asis.virtualcompanion.work.parser.WhatsAppChatParser
import com.asis.virtualcompanion.work.util.ImportUtils
import com.asis.virtualcompanion.work.voice.VoiceNoteProcessor
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Date

class ImportArchiveWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationManager = ProgressNotificationManager(applicationContext)
    private val archiveExtractor = ArchiveExtractor(applicationContext)
    private val chatParser = WhatsAppChatParser()
    private val nlpProcessor = NLPAnalyticsProcessor()
    private val importUtils = ImportUtils(applicationContext)
    private val app = applicationContext as AsisApplication
    private val database = app.database
    private val tflite = app.tflite
    private val voiceProcessor = VoiceNoteProcessor(tflite)

    override suspend fun doWork(): Result {
        val archiveUri = inputData.getString("archive_uri") ?: return Result.failure()
        
        return try {
            notificationManager.startImportNotification()
            
            val archiveUriParsed = android.net.Uri.parse(archiveUri)
            
            // Step 1: Validate archive and storage
            notificationManager.updateProgress("Validating archive", 5, 100)
            
            if (!importUtils.isArchiveValid(archiveUriParsed)) {
                notificationManager.showErrorNotification("Invalid archive file")
                return Result.failure(workDataOf("error" to "Invalid archive file"))
            }
            
            val archiveSize = importUtils.getArchiveSize(archiveUriParsed)
            if (!importUtils.validateStorageSpace(archiveSize)) {
                val sizeStr = importUtils.formatFileSize(archiveSize)
                notificationManager.showErrorNotification("Not enough storage space. Need $sizeStr")
                return Result.failure(workDataOf("error" to "Not enough storage space"))
            }
            
            // Create backup before import
            importUtils.createBackupBeforeImport()
            
            val validation = archiveExtractor.validateArchive(archiveUriParsed)
            if (!validation.isValid) {
                notificationManager.showErrorNotification(validation.error ?: "Invalid archive")
                return Result.failure(workDataOf("error" to validation.error))
            }
            
            // Step 2: Extract archive
            notificationManager.updateProgress("Extracting archive", 15, 100)
            val outputDir = importUtils.getImportDirectory()
            val extractionResult = archiveExtractor.extractArchive(archiveUriParsed, outputDir)
            
            if (!extractionResult.success) {
                notificationManager.showErrorNotification(extractionResult.error ?: "Extraction failed")
                return Result.failure(workDataOf("error" to extractionResult.error))
            }
            
            // Step 3: Parse chat file
            val chatFile = extractionResult.chatFile
            if (chatFile == null || !chatFile.exists()) {
                notificationManager.showErrorNotification("Chat file not found in archive")
                return Result.failure(workDataOf("error" to "Chat file not found"))
            }
            
            notificationManager.updateProgress("Parsing chat messages", 25, 100)
            val chatText = chatFile.readText()
            val chatExport = chatParser.parseChat(chatText)
            
            // Step 4: Save messages to database
            notificationManager.updateProgress("Saving messages", 40, 100, "${chatExport.messages.size} messages")
            saveMessagesToDatabase(chatExport.messages)
            
            // Step 5: Process voice notes
            notificationManager.updateProgress("Processing voice notes", 60, 100, "${chatExport.voiceNotes.size} voice notes")
            processVoiceNotes(chatExport.voiceNotes, outputDir)
            
            // Step 6: Run NLP analytics
            notificationManager.updateProgress("Analyzing conversation patterns", 80, 100)
            val nlpResults = nlpProcessor.analyzeMessages(chatExport.messages)
            savePhraseStatsToDatabase(nlpResults.phraseStats)
            
            // Step 7: Complete
            notificationManager.completeImport(
                messageCount = chatExport.messages.size,
                voiceNoteCount = chatExport.voiceNotes.size,
                phraseCount = nlpResults.phraseStats.size
            )
            
            Result.success(workDataOf(
                "message_count" to chatExport.messages.size,
                "voice_note_count" to chatExport.voiceNotes.size,
                "phrase_count" to nlpResults.phraseStats.size
            ))
            
        } catch (e: Exception) {
            e.printStackTrace()
            notificationManager.showErrorNotification(e.message ?: "Unknown error occurred")
            Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
        } finally {
            // Cleanup temporary files
            try {
                importUtils.cleanupImportDirectory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun saveMessagesToDatabase(messages: List<com.asis.virtualcompanion.data.model.WhatsAppMessage>) {
        val messageDao = database.messageDao()
        val batchSize = 100
        
        messages.chunked(batchSize).forEach { chunk ->
            val entities = chunk.map { msg ->
                MessageEntity(
                    sender = msg.sender,
                    timestamp = msg.timestamp,
                    text = msg.text,
                    sentiment = null, // Will be filled by NLP analysis
                    contextTags = if (msg.isQuoted) listOf("quoted") else emptyList()
                )
            }
            
            entities.forEach { entity ->
                messageDao.insert(entity)
            }
            
            // Update progress
            setProgressAsync(workDataOf("progress" to (40 + (messages.indexOf(chunk.first()) * 20 / messages.size))))
        }
    }
    
    private suspend fun processVoiceNotes(
        voiceNotes: List<com.asis.virtualcompanion.data.model.VoiceNoteMetadata>,
        outputDir: File
    ) {
        val voiceMetaDao = database.voiceMetaDao()
        
        voiceNotes.forEachIndexed { index, voiceNote ->
            try {
                val voiceFile = File(outputDir, voiceNote.fileName)
                if (voiceFile.exists()) {
                    // Extract metadata
                    val metadata = voiceProcessor.extractMetadata(
                        filePath = voiceFile.absolutePath,
                        fileName = voiceNote.fileName,
                        sender = voiceNote.sender,
                        timestamp = voiceNote.timestamp.time
                    )
                    
                    if (metadata != null) {
                        // Classify voice note using TFLite
                        val classification = voiceProcessor.classifyVoiceNote(voiceFile.absolutePath)
                        
                        val entity = VoiceMetaEntity(
                            clipId = metadata.fileName,
                            sender = metadata.sender,
                            emotion = classification?.sentiment,
                            topic = null, // Could be extracted from message context
                            filePath = metadata.filePath,
                            duration = metadata.duration
                        )
                        
                        voiceMetaDao.insert(entity)
                    }
                }
                
                // Update progress
                if (index % 10 == 0) {
                    setProgressAsync(workDataOf("progress" to (60 + (index * 20 / voiceNotes.size))))
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue processing other voice notes even if one fails
            }
        }
    }
    
    private suspend fun savePhraseStatsToDatabase(phraseStats: List<com.asis.virtualcompanion.data.model.PhraseFrequency>) {
        val phraseStatDao = database.phraseStatDao()
        
        phraseStats.chunked(50).forEach { chunk ->
            chunk.forEach { phrase ->
                val entity = PhraseStatEntity(
                    phrase = phrase.phrase,
                    count = phrase.count,
                    emojiHints = phrase.emojiHints
                )
                phraseStatDao.insert(entity)
            }
        }
    }
}
