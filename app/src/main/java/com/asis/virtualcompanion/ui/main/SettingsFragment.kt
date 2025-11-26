package com.asis.virtualcompanion.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.databinding.FragmentSettingsBinding
import com.asis.virtualcompanion.data.preferences.ThemePreferences
import com.asis.virtualcompanion.domain.repository.ThemeRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

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

        binding.privacyPolicyButton.setOnClickListener {
            navigateToPrivacyPolicy()
        }

        binding.clearDataButton.setOnClickListener {
            showClearDataDialog()
        }

        binding.retainVoiceToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVoiceRetentionToggle(isChecked)
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
            }
        }
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
            val fileName = Uri.parse(uri).lastPathSegment ?: ""
            getString(R.string.settings_archive_selected, fileName)
        } ?: getString(R.string.settings_no_archive_selected)

        if (state.archiveParsingInProgress) {
            binding.parsingProgressBar.visibility = View.VISIBLE
            binding.parsingProgressBar.progress = state.archiveParsingProgress
            binding.parsingStatusText.text = getString(
                R.string.settings_parsing_archive_progress,
                state.archiveParsingProgress
            )
            binding.parsingStatusText.visibility = View.VISIBLE
        } else {
            binding.parsingProgressBar.visibility = View.GONE
            binding.parsingStatusText.visibility = View.GONE
        }

        binding.useRealVoiceToggle.isChecked = state.useRealVoice
        binding.processAudioOfflineToggle.isChecked = state.processAudioOffline
        binding.retainVoiceToggle.isChecked = state.retainVoiceRecordings

        state.error?.let {
            binding.errorText.text = it
            binding.errorText.visibility = View.VISIBLE
        } ?: run {
            binding.errorText.visibility = View.GONE
        }
    }

    private fun navigateToPrivacyPolicy() {
        // Navigate to privacy policy fragment
        findNavController().navigate(R.id.action_settings_to_privacy_policy)
    }

    private fun showClearDataDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_clear_all_data))
            .setMessage(getString(R.string.clear_data_confirmation_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.clear)) { dialog, _ ->
                viewModel.clearAllData()
                showToast(getString(R.string.data_cleared_successfully))
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
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