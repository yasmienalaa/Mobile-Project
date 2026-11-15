package com.example.e_learning.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.e_learning.R
import com.example.e_learning.databinding.FragmentEditProfileBinding
import com.example.myapplication.entities.UserEntity

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val vm: AuthViewModel by activityViewModels()
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private var currentUser: UserEntity? = null
    private var filled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentEditProfileBinding.bind(view)
        val fields = ProfileFields(binding.profileFields).also { it.hideRoleSection() }

        vm.observeCurrentUser().observe(viewLifecycleOwner) { user ->
            currentUser = user ?: return@observe
            if (!filled && savedInstanceState == null) {
                binding.etName.setText(user.fullName)
                fields.level = user.level
                fields.languages = user.languageList()
                filled = true
            }
        }

        binding.btnSave.setOnClickListener {
            val user = currentUser ?: return@setOnClickListener
            val name = binding.etName.text.toString()
            val error = Validators.nameError(name)
            binding.tilName.error = error
            if (error != null) return@setOnClickListener

            vm.updateProfile(
                user.copy(
                    fullName = name.trim(),
                    level = fields.level,
                    preferredLanguages = fields.languages.toLanguagesJson()
                )
            ) { findNavController().popBackStack() }
        }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}