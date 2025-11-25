package com.asis.virtualcompanion.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asis.virtualcompanion.AsisApplication
import com.asis.virtualcompanion.common.Result
import com.asis.virtualcompanion.data.database.entity.ChatMessageEntity
import com.asis.virtualcompanion.data.model.MemeGenerationConfig
import com.asis.virtualcompanion.data.model.MemeMode
import com.asis.virtualcompanion.di.AppModule
import com.asis.virtualcompanion.domain.MemeGenerator
import com.asis.virtualcompanion.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessageEntity>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

data class SendMessageState(
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val chatMessageRepository: ChatMessageRepository = 
        AppModule.provideChatMessageRepository(application)
    
    private val memeGenerator: MemeGenerator = 
        AppModule.provideMemeGenerator(application)
    
    private val _uiState = MutableLiveData<ChatUiState>(ChatUiState.Loading)
    val uiState: LiveData<ChatUiState> = _uiState
    
    private val _sendMessageState = MutableLiveData(SendMessageState())
    val sendMessageState: LiveData<SendMessageState> = _sendMessageState
    
    private val _scrollToBottom = MutableLiveData<Unit>()
    val scrollToBottom: LiveData<Unit> = _scrollToBottom
    
    init {
        loadMessages()
    }
    
    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            
            chatMessageRepository.getAllChatMessages()
                .catch { e ->
                    _uiState.value = ChatUiState.Error(e.message ?: "Failed to load messages")
                }
                .collect { messages ->
                    _uiState.value = ChatUiState.Success(messages)
                }
        }
    }
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            _sendMessageState.value = SendMessageState(isSending = true)
            
            // Save user message
            val userMessage = ChatMessageEntity(
                message = text,
                isFromUser = true,
                timestamp = Date()
            )
            
            val insertResult = chatMessageRepository.insertChatMessage(userMessage)
            
            if (insertResult is Result.Error) {
                _sendMessageState.value = SendMessageState(
                    isSending = false,
                    error = "Failed to send message"
                )
                return@launch
            }
            
            // Trigger scroll to bottom after user message is sent
            _scrollToBottom.value = Unit
            
            // Generate response
            generateResponse(text)
        }
    }
    
    private suspend fun generateResponse(userMessage: String) {
        val config = MemeGenerationConfig(
            mode = MemeMode.CONTEXT_AWARE,
            currentTime = System.currentTimeMillis(),
            includeAudio = false,
            variability = 0.7f
        )
        
        val responseResult = memeGenerator.generateMemeResponse(config)
        
        when (responseResult) {
            is Result.Success -> {
                val response = responseResult.data
                val companionMessage = ChatMessageEntity(
                    message = response.text,
                    isFromUser = false,
                    timestamp = Date(),
                    metadata = mapOf(
                        "confidence" to response.confidence.toString(),
                        "template_id" to response.template.id
                    )
                )
                
                val insertResult = chatMessageRepository.insertChatMessage(companionMessage)
                
                when (insertResult) {
                    is Result.Success -> {
                        _sendMessageState.value = SendMessageState(isSending = false)
                        _scrollToBottom.value = Unit
                    }
                    is Result.Error -> {
                        _sendMessageState.value = SendMessageState(
                            isSending = false,
                            error = "Failed to save response"
                        )
                    }
                }
            }
            is Result.Error -> {
                _sendMessageState.value = SendMessageState(
                    isSending = false,
                    error = "Failed to generate response"
                )
            }
        }
    }
    
    fun retry() {
        loadMessages()
    }
    
    fun clearError() {
        _sendMessageState.value = SendMessageState()
    }
}
