package com.asis.virtualcompanion.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.data.model.AudioPlaybackState
import com.asis.virtualcompanion.data.model.VoiceInteractionMode
import com.asis.virtualcompanion.databinding.FragmentVoiceBinding
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class VoiceFragment : Fragment() {

    private var _binding: FragmentVoiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VoiceViewModel by viewModels()
    private var recordingActive = false
    private var playbackSeekbarListener: SeekBar.OnSeekBarChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.modeToggleButton.setOnClickListener {
            viewModel.toggleInteractionMode()
        }

        binding.useRealVoiceSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseRealVoice(isChecked)
        }

        val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        binding.micButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.micButton.startAnimation(pulseAnimation)
                    recordingActive = false
                    binding.micButton.postDelayed({
                        viewModel.startRecording()
                        recordingActive = true
                        binding.visualizationCard.isVisible = true
                        binding.statusText.text = getString(R.string.voice_recording_hint)
                    }, 200)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.micButton.clearAnimation()
                    if (recordingActive) {
                        viewModel.stopRecording()
                        binding.visualizationCard.isVisible = false
                        binding.statusText.text = getString(R.string.voice_press_hint)
                    }
                    recordingActive = false
                    true
                }
                else -> false
            }
        }

        playbackSeekbarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.playbackTimeText.text = formatDuration(progress) +
                            " / " + formatDuration(seekBar?.max ?: 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.seekTo(it.progress)
                }
            }
        }

        binding.playbackSeekBar.setOnSeekBarChangeListener(playbackSeekbarListener)

        binding.playPauseButton.setOnClickListener {
            val currentState = viewModel.uiState.value?.playbackInfo?.state
            if (currentState == AudioPlaybackState.PLAYING) {
                viewModel.pause()
            } else {
                viewModel.play()
            }
        }

        binding.replayButton.setOnClickListener {
            viewModel.replay()
        }

        binding.dismissErrorButton.setOnClickListener {
            viewModel.clearError()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateModeButton(state.interactionMode)
            binding.useRealVoiceSwitch.isChecked = state.useRealVoiceSnippets
            updateRecordingState(state.recordingState)
            updateProcessingState(state.isProcessing, state.processingMessage)
            updatePlaybackState(state.playbackInfo, state.lastMemeResponse?.text)
            updateError(state.errorMessage)
        }
    }

    private fun updateModeButton(mode: VoiceInteractionMode) {
        when (mode) {
            VoiceInteractionMode.RANDOM_MEME -> {
                binding.modeToggleButton.text = getString(R.string.voice_mode_random)
            }
            VoiceInteractionMode.EMOTION_RESPONSE -> {
                binding.modeToggleButton.text = getString(R.string.voice_mode_emotion)
            }
        }
    }

    private fun updateRecordingState(recordingState: com.asis.virtualcompanion.data.model.AudioRecordingState) {
        binding.visualizationCard.isVisible = recordingState.isRecording
        binding.amplitudeIndicator.progress = recordingState.amplitudePercent.toInt()
        binding.recordingDurationText.text = formatDuration(recordingState.durationMs.toInt())
        binding.statusText.text = if (recordingState.isRecording) {
            getString(R.string.voice_recording_hint)
        } else {
            getString(R.string.voice_press_hint)
        }
    }

    private fun updateProcessingState(isProcessing: Boolean, message: String) {
        binding.processingIndicator.isVisible = isProcessing
        binding.processingText.isVisible = isProcessing
        binding.processingText.text = message
        binding.micButton.isEnabled = !isProcessing
    }

    private fun updatePlaybackState(
        playbackInfo: com.asis.virtualcompanion.data.model.AudioPlaybackInfo,
        responseText: String?
    ) {
        val hasResponse = responseText != null
        binding.playbackCard.isVisible = hasResponse ||
                playbackInfo.state != AudioPlaybackState.IDLE

        binding.responseText.text = responseText ?: getString(R.string.voice_no_response)

        val duration = max(playbackInfo.durationMs, 1)
        binding.playbackSeekBar.max = duration
        binding.playbackSeekBar.progress = min(playbackInfo.currentPositionMs, duration)
        binding.playbackTimeText.text = formatDuration(playbackInfo.currentPositionMs) +
                " / " + formatDuration(playbackInfo.durationMs)

        val isPlaying = playbackInfo.state == AudioPlaybackState.PLAYING
        binding.playPauseButton.text = if (isPlaying) {
            getString(R.string.voice_pause)
        } else {
            getString(R.string.voice_play)
        }
        binding.playPauseButton.icon = ContextCompat.getDrawable(
            requireContext(),
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
    }

    private fun updateError(errorMessage: String?) {
        binding.errorCard.isVisible = errorMessage != null
        binding.errorText.text = errorMessage
    }

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playbackSeekBar.setOnSeekBarChangeListener(null)
        _binding = null
    }
}
