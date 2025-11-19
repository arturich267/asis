package com.asis.virtualcompanion.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.asis.virtualcompanion.databinding.FragmentChatBinding
import com.asis.virtualcompanion.ui.chat.ChatMessageAdapter
import com.asis.virtualcompanion.ui.chat.ChatUiState
import com.asis.virtualcompanion.ui.chat.ChatViewModel
import com.google.android.material.snackbar.Snackbar

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter()
        binding.messagesRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        
        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }
    
    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatUiState.Loading -> {
                    showLoading()
                }
                is ChatUiState.Success -> {
                    showMessages(state.messages)
                }
                is ChatUiState.Error -> {
                    showError(state.message)
                }
            }
        }
        
        viewModel.sendMessageState.observe(viewLifecycleOwner) { state ->
            binding.sendButton.isEnabled = !state.isSending
            binding.messageInput.isEnabled = !state.isSending
            
            state.error?.let { error ->
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.scrollToBottom.observe(viewLifecycleOwner) {
            scrollToBottom()
        }
    }
    
    private fun sendMessage() {
        val text = binding.messageInput.text?.toString() ?: ""
        if (text.isNotBlank()) {
            viewModel.sendMessage(text)
            binding.messageInput.text?.clear()
        }
    }
    
    private fun showLoading() {
        binding.messagesRecyclerView.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.loadingIndicator.visibility = View.VISIBLE
    }
    
    private fun showMessages(messages: List<com.asis.virtualcompanion.data.database.entity.ChatMessageEntity>) {
        binding.loadingIndicator.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        
        if (messages.isEmpty()) {
            binding.messagesRecyclerView.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.messagesRecyclerView.visibility = View.VISIBLE
            chatAdapter.submitList(messages)
            
            // Scroll to bottom when new messages arrive
            binding.messagesRecyclerView.postDelayed({
                scrollToBottom()
            }, 100)
        }
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.messagesRecyclerView.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.errorState.visibility = View.VISIBLE
        binding.errorText.text = message
    }
    
    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            binding.messagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
