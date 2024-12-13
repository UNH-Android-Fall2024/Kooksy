package com.teamfour.kooksy.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teamfour.kooksy.databinding.FragmentProfileDetailsBinding

// Fragment to handle user profile details, including updating username and password
class FragmentProfileDetails : Fragment() {

    lateinit var binding: FragmentProfileDetailsBinding
    private val regex: Regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$".toRegex() //// Regex to validate strong passwords
    val viewModel: ProfileViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Populate fields with existing user details, if available
        UserDetails.user?.let {
            binding.emailEditText.setText(UserDetails.user?.email)
            binding.nameEditText.setText(UserDetails.user?.user_name)
        }

        // Variables to store input values
        var password = ""
        var confirmPassword =""
        var username  = ""
        binding.nameEditText.doAfterTextChanged {  username = it.toString() }
        binding.passwordEditText.doAfterTextChanged { password = it.toString() }
        binding.confirmPasswordEditText.doAfterTextChanged { confirmPassword = it.toString() }

        binding.updateButton.setOnClickListener {
            when {
                UserDetails.user?.user_name.equals(username) -> Toast.makeText(requireActivity(), "User name cannot be same as previous", Toast.LENGTH_SHORT).show()
                username.length < 3 || username.length > 20 || !username.matches("^[a-zA-Z0-9_]+$".toRegex()) -> Toast.makeText(requireActivity(), "Invalid username", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(requireActivity(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                password.matches(regex).not() -> Toast.makeText(requireActivity(), "Weak password", Toast.LENGTH_SHORT).show()
                password != confirmPassword -> Toast.makeText(requireActivity(), "Passwords don't match", Toast.LENGTH_SHORT).show()
                else -> viewModel.updateProfileDetails(username, UserDetails.user!!)
            }
        }

        viewModel.liveData.observe(viewLifecycleOwner) {
            if(it){
                Toast.makeText(requireActivity(), "Username updated successfully", Toast.LENGTH_SHORT).show()
                UserDetails.user?.user_name = username
                findNavController().popBackStack()
            }
        }
    }
}