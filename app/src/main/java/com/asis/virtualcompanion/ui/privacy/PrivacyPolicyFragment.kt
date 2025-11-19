package com.asis.virtualcompanion.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asis.virtualcompanion.R
import com.asis.virtualcompanion.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        binding.privacyLastUpdated.text = getString(
            R.string.privacy_policy_last_updated,
            "Nov 2023"
        )
    }

    private fun setupToolbar() {
        binding.privacyToolbar.apply {
            navigationIcon = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_close
            )
            navigationContentDescription = getString(R.string.privacy_policy_close_content_description)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
