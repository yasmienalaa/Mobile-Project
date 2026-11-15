package com.example.e_learning.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.e_learning.R
import com.example.e_learning.databinding.FragmentHomePlaceholderBinding

abstract class PlaceholderHomeFragment(private val title: String) :
    Fragment(R.layout.fragment_home_placeholder) {

    private val vm: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentHomePlaceholderBinding.bind(view)
        binding.tvTitle.text = title
        binding.btnProfile.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        binding.btnLogout.setOnClickListener { vm.logout(); goToLogin() }
    }
}

class StudentHomeFragment : PlaceholderHomeFragment("Student Home")
class AdminHomeFragment : PlaceholderHomeFragment("Admin Home")