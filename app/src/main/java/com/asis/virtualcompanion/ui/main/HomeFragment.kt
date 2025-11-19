package com.asis.virtualcompanion.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.databinding.FragmentHomeBinding

/**
 * Home fragment with voice and chat interaction buttons
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.surfaceState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }

    private fun setupClickListeners() {
        binding.settingsButton.setOnClickListener {
            viewModel.onSettingsClicked()
            navigateToSettings()
        }
        
        binding.voiceButton.setOnClickListener {
            viewModel.onVoiceClicked()
            navigateToVoice()
        }
        
        binding.chatButton.setOnClickListener {
            viewModel.onChatClicked()
            navigateToChat()
        }
    }

    private fun updateUI(state: HomeSurfaceState) {
        // Update background if needed
        binding.root.setBackgroundResource(state.currentBackgroundRes)
        
        // Update profile summary if available
        if (state.lastProfileSummary.isNotEmpty()) {
            binding.profileSummary.text = state.lastProfileSummary
            binding.profileSummary.visibility = View.VISIBLE
        } else {
            binding.profileSummary.visibility = View.GONE
        }
        
        // Update button availability
        binding.voiceButton.isEnabled = state.isVoiceAvailable
        binding.chatButton.isEnabled = state.isChatAvailable
    }

    private fun navigateToSettings() {
        findNavController().navigate(R.id.action_home_to_settings)
    }

    private fun navigateToVoice() {
        findNavController().navigate(R.id.action_home_to_voice)
    }

    private fun navigateToChat() {
        findNavController().navigate(R.id.action_home_to_chat)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}