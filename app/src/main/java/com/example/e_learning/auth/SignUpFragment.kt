package com.example.e_learning.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.e_learning.R
import com.example.e_learning.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val vm: AuthViewModel by activityViewModels()
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSignUpBinding.bind(view)
        if (savedInstanceState == null) vm.consumeState()
        val fields = ProfileFields(binding.profileFields)

        binding.btnSignUp.setOnClickListener {
            if (validate()) {
                vm.signUp(
                    fullName = binding.etName.text.toString(),
                    email = binding.etEmail.text.toString(),
                    password = binding.etPassword.text.toString(),
                    role = fields.role,
                    level = fields.level,
                    languages = fields.languages
                )
            }
        }
        binding.btnBackToLogin.setOnClickListener { findNavController().popBackStack() }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    private fun validate(): Boolean {
        val password = binding.etPassword.text.toString()
        val checks = listOf(
            binding.tilName to Validators.nameError(binding.etName.text.toString()),
            binding.tilEmail to Validators.emailError(binding.etEmail.text.toString()),
            binding.tilPassword to Validators.passwordError(password),
            binding.tilConfirm to Validators.confirmError(password, binding.etConfirm.text.toString())
        )
        checks.forEach { (layout, error) -> layout.error = error }
        return checks.all { it.second == null }
    }

    private fun render(state: AuthState) {
        val loading = state is AuthState.Loading
        binding.progress.isVisible = loading
        binding.btnSignUp.isEnabled = !loading
        binding.tvError.isVisible = state is AuthState.Error
        when (state) {
            is AuthState.Error -> binding.tvError.text = state.message
            is AuthState.Success -> { vm.consumeState(); goHome(state.user.role) }
            else -> Unit
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}