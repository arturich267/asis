package com.asis.virtualcompanion.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.data.model.VoiceRetentionPolicy
import com.asis.virtualcompanion.databinding.FragmentSettingsBinding
import com.asis.virtualcompanion.data.preferences.ThemePreferences
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel
    private var suppressRetentionToggle = false

    private val archivePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.selectArchive(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel()
        setupUI()
        observeViewModel()
    }

    private fun initializeViewModel() {
        val themePreferences = ThemePreferences(requireContext())
        val themeRepository = ThemeRepository(requireContext(), themePreferences)
        val factory = SettingsViewModelFactory(requireContext(), themeRepository)
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
    }

    private fun setupUI() {
        binding.archivePickerButton.setOnClickListener {
            archivePickerLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
        }

        binding.useRealVoiceToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleUseRealVoice(isChecked)
        }

        binding.processAudioOfflineToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleProcessAudioOffline(isChecked)
        }

        binding.voiceRetentionToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || suppressRetentionToggle) return@addOnButtonCheckedListener
            val policy = when (checkedId) {
                R.id.retain_recordings_button -> VoiceRetentionPolicy.RETAIN
                else -> VoiceRetentionPolicy.DELETE_IMMEDIATELY
            }
            viewModel.updateVoiceRetentionPolicy(policy)
        }

        binding.revokeExternalAccessButton.setOnClickListener {
            viewModel.revokeArchiveAccess()
        }

        binding.privacyPolicyButton.setOnClickListener {
            viewModel.openPrivacyPolicy()
        }

        binding.clearDataButton.setOnClickListener {
            showClearDataConfirmation()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUIState(state)
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is SettingsViewModel.NavigationEvent.SelectArchive -> {
                    archivePickerLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                }
                is SettingsViewModel.NavigationEvent.PrivacyPolicy -> {
                    findNavController().navigate(R.id.action_settings_to_privacyPolicy)
                }
            }
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.onStatusMessageShown()
            }
        }
    }

    private fun showClearDataConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_clear_data_title)
            .setMessage(R.string.settings_clear_data_message)
            .setPositiveButton(R.string.settings_clear_data_confirm) { _, _ ->
                viewModel.clearAllData()
            }
            .setNegativeButton(R.string.settings_clear_data_cancel, null)
            .show()
    }

    private fun formatArchiveLabel(uri: String): String {
        return Uri.parse(uri).lastPathSegment ?: uri
    }

    private fun updateUIState(state: SettingsUiState) {
        binding.themeContainer.removeAllViews()
        state.themes.forEach { theme ->
            val themeButton = com.google.android.material.button.MaterialButton(
                requireContext(),
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = theme.name
                isCheckable = true
                isChecked = theme.id == state.selectedThemeId
                setOnClickListener {
                    viewModel.selectTheme(theme.id)
                }
            }
            binding.themeContainer.addView(themeButton)
        }

        binding.archiveStatusText.text = state.archiveUri?.let { uri ->
            getString(R.string.settings_archive_selected, formatArchiveLabel(uri))
        } ?: getString(R.string.settings_no_archive_selected)

        if (state.archiveParsingInProgress) {
            binding.parsingProgressBar.visibility = View.VISIBLE
            binding.parsingProgressBar.progress = state.archiveParsingProgress
            binding.parsingStatusText.visibility = View.VISIBLE
            binding.parsingStatusText.text = getString(
                R.string.settings_parsing_archive_progress,
                state.archiveParsingProgress
            )
        } else {
            binding.parsingProgressBar.visibility = View.GONE
            binding.parsingStatusText.visibility = View.GONE
        }

        val hasExternalArchive = state.archiveUri != null
        binding.externalStorageIndicatorCard.isVisible = hasExternalArchive
        binding.revokeExternalAccessButton.isEnabled = hasExternalArchive
        if (hasExternalArchive) {
            binding.externalStorageStatusText.text = getString(
                R.string.settings_external_access_description,
                formatArchiveLabel(state.archiveUri!!)
            )
        }

        if (binding.useRealVoiceToggle.isChecked != state.useRealVoice) {
            binding.useRealVoiceToggle.isChecked = state.useRealVoice
        }

        if (binding.processAudioOfflineToggle.isChecked != state.processAudioOffline) {
            binding.processAudioOfflineToggle.isChecked = state.processAudioOffline
        }

        val retentionButtonId = when (state.voiceRetentionPolicy) {
            VoiceRetentionPolicy.RETAIN -> R.id.retain_recordings_button
            VoiceRetentionPolicy.DELETE_IMMEDIATELY -> R.id.delete_recordings_button
        }
        if (binding.voiceRetentionToggleGroup.checkedButtonId != retentionButtonId) {
            suppressRetentionToggle = true
            binding.voiceRetentionToggleGroup.check(retentionButtonId)
            suppressRetentionToggle = false
        }

        binding.clearDataButton.isEnabled = !state.isClearingData
        binding.clearDataButton.text = getString(
            if (state.isClearingData) R.string.settings_clearing_data else R.string.settings_clear_data
        )

        state.error?.let {
            binding.errorText.text = it
            binding.errorText.visibility = View.VISIBLE
        } ?: run {
            binding.errorText.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SettingsViewModelFactory(
    private val context: android.content.Context,
    private val themeRepository: ThemeRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(context, themeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}