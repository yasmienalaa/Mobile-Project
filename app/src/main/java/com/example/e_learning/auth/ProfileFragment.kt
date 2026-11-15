package com.example.e_learning.auth

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.e_learning.R
import com.example.e_learning.databinding.FragmentProfileBinding
import com.example.myapplication.entities.UserEntity
import com.google.android.material.chip.Chip

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val vm: AuthViewModel by activityViewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)

        binding.btnEdit.setOnClickListener { findNavController().navigate(R.id.editProfileFragment) }
        binding.btnLogout.setOnClickListener { vm.logout(); goToLogin() }

        vm.observeCurrentUser().observe(viewLifecycleOwner) { user -> user?.let(::show) }
    }

    private fun show(user: UserEntity) = with(binding) {
        tvAvatar.text = user.fullName.take(1).uppercase()
        tvName.text = user.fullName
        tvEmail.text = user.email
        tvRole.text = user.role.pretty().uppercase()
        tvLevel.text = user.level.pretty()

        chipsLanguages.removeAllViews()
        user.languageList().ifEmpty { listOf("Not selected") }
            .forEach { chipsLanguages.addView(languageChip(it)) }
    }

    private fun languageChip(label: String) = Chip(requireContext()).apply {
        text = label
        isClickable = false
        isCheckable = false
        chipStrokeWidth = 0f
        setChipBackgroundColorResource(R.color.sage_accent)
        setTextColor(ContextCompat.getColor(context, R.color.sage_text))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}