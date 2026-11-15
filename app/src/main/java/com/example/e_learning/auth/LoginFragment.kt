package com.example.e_learning.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.e_learning.R
import com.example.e_learning.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val vm: AuthViewModel by activityViewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)
        if (savedInstanceState == null) vm.consumeState()

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val emailError = Validators.emailError(email)
            val passwordError = if (password.isEmpty()) "Password is required" else null
            binding.tilEmail.error = emailError
            binding.tilPassword.error = passwordError
            if (emailError == null && passwordError == null) vm.login(email, password)
        }
        binding.btnGoSignUp.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: AuthState) {
        val loading = state is AuthState.Loading
        binding.progress.isVisible = loading
        binding.btnSignIn.isEnabled = !loading
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