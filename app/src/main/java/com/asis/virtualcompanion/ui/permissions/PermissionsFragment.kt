package com.asis.virtualcompanion.ui.permissions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.data.preferences.PermissionsPreferences
import com.asis.virtualcompanion.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PermissionsViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionArray = permissions.keys.toTypedArray()
        val grantResults = IntArray(permissions.size) { index ->
            val result = permissions.values.elementAt(index)
            if (result) android.content.pm.PackageManager.PERMISSION_GRANTED
            else android.content.pm.PackageManager.PERMISSION_DENIED
        }
        viewModel.handlePermissionsResult(permissionArray, grantResults)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val preferencesRepository = PermissionsPreferences(context)
        viewModel = PermissionsViewModel(context, preferencesRepository)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.grantButton.setOnClickListener {
            viewModel.requestPermissions()
        }
    }

    private fun observeViewModel() {
        viewModel.permissionsGranted.observe(viewLifecycleOwner) { granted ->
            if (granted) {
                navigateToHome()
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PermissionsViewModel.NavigationEvent.NavigateToHome -> navigateToHome()
                is PermissionsViewModel.NavigationEvent.RequestPermissions -> {
                    permissionLauncher.launch(event.permissions)
                }
                is PermissionsViewModel.NavigationEvent.LaunchSettings -> {
                    startActivity(event.intent)
                }
            }
        }

        viewModel.rationaleDialog.observe(viewLifecycleOwner) { rationaleData ->
            if (rationaleData != null) {
                showRationaleDialog(rationaleData)
            }
        }

        viewModel.denialDialog.observe(viewLifecycleOwner) { show ->
            if (show) {
                showDenialDialog()
                viewModel.dismissDenialDialog()
            }
        }
    }

    private fun showRationaleDialog(rationaleData: PermissionsViewModel.RationaleDialogData) {
        val message = rationaleData.rationales.joinToString("\n\n") { rationale ->
            "${rationale.title}: ${rationale.description}"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.permissions_rationale_title)
            .setMessage(message)
            .setPositiveButton(R.string.permissions_grant_button) { _, _ ->
                viewModel.proceedAfterRationale()
                viewModel.dismissRationaleDialog()
            }
            .setNegativeButton(R.string.permissions_cancel) { _, _ ->
                viewModel.dismissRationaleDialog()
            }
            .show()
    }

    private fun showDenialDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.permissions_denied_title)
            .setMessage(R.string.permissions_denied_message)
            .setPositiveButton(R.string.permissions_go_to_settings) { _, _ ->
                viewModel.goToSettings()
            }
            .setNegativeButton(R.string.permissions_cancel, null)
            .show()
    }

    private fun navigateToHome() {
        val navController = findNavController()
        navController.navigate(R.id.action_permissions_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
