package com.asis.virtualcompanion.work.parser

import com.asis.virtualcompanion.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class WhatsAppChatParser {
    
    private val dateFormats = listOf(
        "M/d/yy, h:mm a",           // US format: 12/31/23, 11:59 PM
        "d/M/yy, h:mm a",           // European format: 31/12/23, 11:59 PM
        "dd/MM/yyyy, HH:mm",        // 24-hour format: 31/12/2023, 23:59
        "MM/dd/yyyy, h:mm a",       // Full US format: 12/31/2023, 11:59 PM
        "d MMM yyyy, HH:mm",        // International: 31 Dec 2023, 23:59
        "MMM dd, yyyy, h:mm a"      // Short month: Dec 31, 2023, 11:59 PM
    )
    
    private val messagePattern = Pattern.compile(
        """^(\d{1,2}[/-]\d{1,2}[/-]\d{2,4},?\s+\d{1,2}:\d{2}(?:\s*[AP]M)?)\s+([^:]+):\s*(.*)$"""
    )
    
    private val systemMessagePattern = Pattern.compile(
        """^(\d{1,2}[/-]\d{1,2}[/-]\d{2,4},?\s+\d{1,2}:\d{2}(?:\s*[AP]M)?)\s+([^:]+)$"""
    )
    
    private val mediaPatterns = mapOf(
        MessageType.IMAGE to listOf(
            Pattern.compile("<image: omitted>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("IMG-\\d{4}-\\d{2}-\\d{2}-WA\\d+\\.jpg", Pattern.CASE_INSENSITIVE)
        ),
        MessageType.VIDEO to listOf(
            Pattern.compile("<video: omitted>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("VID-\\d{4}-\\d{2}-\\d{2}-WA\\d+\\.mp4", Pattern.CASE_INSENSITIVE)
        ),
        MessageType.AUDIO to listOf(
            Pattern.compile("<audio: omitted>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("AUD-\\d{4}-\\d{2}-\\d{2}-WA\\d+\\.opus", Pattern.CASE_INSENSITIVE)
        ),
        MessageType.VOICE_NOTE to listOf(
            Pattern.compile("<Media omitted>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("PTT-\\d{4}-\\d{2}-\\d{2}-WA\\d+\\.opus", Pattern.CASE_INSENSITIVE)
        ),
        MessageType.DOCUMENT to listOf(
            Pattern.compile("<document: omitted>", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt)$", Pattern.CASE_INSENSITIVE)
        ),
        MessageType.STICKER to listOf(
            Pattern.compile("<sticker: omitted>", Pattern.CASE_INSENSITIVE)
        )
    )
    
    private val quotedMessagePattern = Pattern.compile("""^[«"'](.+)[»"']\s*$""")
    
    fun parseChat(chatText: String): WhatsAppChatExport {
        val lines = chatText.split("\n")
        val messages = mutableListOf<WhatsAppMessage>()
        var currentMessage: WhatsAppMessage? = null
        
        for (line in lines) {
            if (line.trim().isEmpty()) continue
            
            val message = parseLine(line.trim())
            if (message != null) {
                if (currentMessage != null) {
                    messages.add(currentMessage)
                }
                currentMessage = message
            } else if (currentMessage != null && currentMessage.messageType == MessageType.TEXT) {
                // Append multiline text content
                val updatedText = currentMessage.text + "\n" + line.trim()
                currentMessage = currentMessage.copy(text = updatedText)
            }
        }
        
        // Add the last message
        currentMessage?.let { messages.add(it) }
        
        return WhatsAppChatExport(
            messages = messages,
            mediaFiles = extractMediaFiles(messages),
            voiceNotes = extractVoiceNotes(messages)
        )
    }
    
    private fun parseLine(line: String): WhatsAppMessage? {
        // Try regular message pattern first
        val messageMatcher = messagePattern.matcher(line)
        if (messageMatcher.find()) {
            val dateStr = messageMatcher.group(1)
            val sender = messageMatcher.group(2).trim()
            val content = messageMatcher.group(3).trim()
            
            val timestamp = parseTimestamp(dateStr) ?: return null
            val messageType = detectMessageType(content)
            val isQuoted = detectQuotedMessage(content)
            val quotedMessage = if (isQuoted) extractQuotedMessage(content) else null
            val mediaFileName = extractMediaFileName(content, messageType)
            val voiceNoteDuration = extractVoiceNoteDuration(content)
            
            return WhatsAppMessage(
                sender = sender,
                timestamp = timestamp,
                text = if (messageType == MessageType.TEXT) content else "",
                messageType = messageType,
                isQuoted = isQuoted,
                quotedMessage = quotedMessage,
                mediaFileName = mediaFileName,
                voiceNoteDuration = voiceNoteDuration
            )
        }
        
        // Try system message pattern
        val systemMatcher = systemMessagePattern.matcher(line)
        if (systemMatcher.find()) {
            val dateStr = systemMatcher.group(1)
            val content = systemMatcher.group(2).trim()
            
            val timestamp = parseTimestamp(dateStr) ?: return null
            
            return WhatsAppMessage(
                sender = "System",
                timestamp = timestamp,
                text = content,
                messageType = MessageType.SYSTEM
            )
        }
        
        return null
    }
    
    private fun parseTimestamp(dateStr: String): Date? {
        val normalizedDateStr = dateStr.replace(Regex("""\s+"""), " ").trim()
        
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = true
                return sdf.parse(normalizedDateStr)
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        
        // Try additional parsing for edge cases
        return try {
            val parts = normalizedDateStr.split(",")
            if (parts.size >= 2) {
                val datePart = parts[0].trim()
                val timePart = parts[1].trim()
                parseFlexibleDate(datePart, timePart)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseFlexibleDate(datePart: String, timePart: String): Date? {
        // Additional flexible parsing for various date formats
        val datePatterns = listOf(
            """(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})""",
            """(\d{1,2})\s+(\w{3})\s+(\d{4})""",
            """(\w{3})\s+(\d{1,2}),?\s+(\d{4})"""
        )
        
        for (pattern in datePatterns) {
            try {
                val matcher = Pattern.compile(pattern).matcher(datePart)
                if (matcher.find()) {
                    // Build a flexible date parser here
                    // For now, return null and let the main formats handle it
                }
            } catch (e: Exception) {
                // Continue
            }
        }
        
        return null
    }
    
    private fun detectMessageType(content: String): MessageType {
        for ((type, patterns) in mediaPatterns) {
            for (pattern in patterns) {
                if (pattern.matcher(content).find()) {
                    return type
                }
            }
        }
        
        // Check for location messages
        if (content.contains("location") || content.contains("http://maps.google.com")) {
            return MessageType.LOCATION
        }
        
        // Check for contact messages
        if (content.contains("contact") || content.contains("vcard")) {
            return MessageType.CONTACT
        }
        
        return MessageType.TEXT
    }
    
    private fun detectQuotedMessage(content: String): Boolean {
        return quotedMessagePattern.matcher(content.trim()).find() ||
               content.startsWith(">") ||
               content.contains("quoted message")
    }
    
    private fun extractQuotedMessage(content: String): String? {
        val matcher = quotedMessagePattern.matcher(content.trim())
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            val lines = content.split("\n")
            lines.firstOrNull { it.startsWith(">") }?.substring(1)?.trim()
        }
    }
    
    private fun extractMediaFileName(content: String, messageType: MessageType): String? {
        if (messageType == MessageType.TEXT) return null
        
        // Try to extract actual filename from content
        val filePattern = Pattern.compile("""[\w-]+\.(jpg|jpeg|png|gif|mp4|mov|avi|opus|mp3|wav|pdf|doc|docx|xls|xlsx|ppt|pptx|txt)""", Pattern.CASE_INSENSITIVE)
        val matcher = filePattern.matcher(content)
        return if (matcher.find()) matcher.group() else null
    }
    
    private fun extractVoiceNoteDuration(content: String): Long? {
        // Try to extract duration from content if available
        val durationPattern = Pattern.compile("""\((\d+):(\d+)\)""")
        val matcher = durationPattern.matcher(content)
        return if (matcher.find()) {
            val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
            val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
            (minutes * 60 + seconds) * 1000 // Convert to milliseconds
        } else null
    }
    
    private fun extractMediaFiles(messages: List<WhatsAppMessage>): List<String> {
        return messages
            .mapNotNull { it.mediaFileName }
            .distinct()
    }
    
    private fun extractVoiceNotes(messages: List<WhatsAppMessage>): List<VoiceNoteMetadata> {
        return messages
            .filter { it.messageType == MessageType.VOICE_NOTE }
            .mapNotNull { msg ->
                msg.mediaFileName?.let { fileName ->
                    VoiceNoteMetadata(
                        fileName = fileName,
                        sender = msg.sender,
                        timestamp = msg.timestamp,
                        duration = msg.voiceNoteDuration ?: 0L,
                        filePath = "" // Will be filled during file extraction
                    )
                }
            }
    }
}