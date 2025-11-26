package com.asis.virtualcompanion.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.databinding.FragmentDashboardBinding

/**
 * Dashboard fragment placeholder
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.textDashboard.text = getString(R.string.dashboard_placeholder_text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}