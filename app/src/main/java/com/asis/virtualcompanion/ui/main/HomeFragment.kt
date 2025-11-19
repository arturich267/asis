package com.asis.virtualcompanion.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.common.UiState
import com.asis.virtualcompanion.databinding.FragmentHomeBinding

/**
 * Home fragment showing welcome screen
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels()

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
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }
                is UiState.Success -> {
                    showLoading(false)
                    showContent(state.data)
                }
                is UiState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is UiState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.contentRefreshButton.setOnClickListener {
            viewModel.refreshData()
        }
        binding.errorRefreshButton.setOnClickListener {
            viewModel.refreshData()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (show) View.GONE else View.VISIBLE
        binding.errorGroup.visibility = View.GONE
    }

    private fun showContent(message: String) {
        binding.contentGroup.visibility = View.VISIBLE
        binding.errorGroup.visibility = View.GONE
        binding.welcomeTitle.text = getString(R.string.welcome_title)
        binding.welcomeSubtitle.text = message
    }

    private fun showError(message: String) {
        binding.contentGroup.visibility = View.GONE
        binding.errorGroup.visibility = View.VISIBLE
        binding.errorMessage.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}