package com.teamfour.kooksy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.teamfour.kooksy.R
import com.teamfour.kooksy.databinding.FragmentFilterOverlayBinding

class FilterOverlayFragment(
    private val applyFilters: (String, Boolean, Int) -> Unit, //To apply filters with the selected criteria
    private val clearFilters: () -> Unit // To clear filters
) : DialogFragment() {

    private var _binding: FragmentFilterOverlayBinding? = null
    private val binding get() = _binding!!

    // Variables to hold selected filter values
    private var selectedDishType = "Both"
    private var selectedDifficulty = "All"
    private var selectedRating = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterOverlayBinding.inflate(inflater, container, false)
        val view = binding.root

        // Slide-in animation to the filter overlay
        val slideInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        view.startAnimation(slideInAnimation)

        binding.dishTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDishType = when (checkedId) {
                R.id.radio_without_meat -> "Without Meat"
                R.id.radio_with_meat -> "Meat"
                else -> "Both"
            }
        }

        binding.difficultyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDifficulty = when (checkedId) {
                R.id.radio_easy -> "Easy"
                R.id.radio_medium -> "Medium"
                R.id.radio_hard -> "Hard"
                else -> "All"
            }
        }

        // Setup for the rating slider (1 to 5 stars)
        binding.ratingSeekBar.max = 4
        binding.ratingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedRating = progress + 1
                binding.ratingValue.text = selectedRating.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Apply Filter button
        binding.applyFilterButton.setOnClickListener {
            applyFilters(selectedDifficulty, selectedDishType == "Without Meat", selectedRating)
            dismiss()
        }

        // Clear Filter button
        binding.clearFilterButton.setOnClickListener {
            clearFilters()
            dismiss()
        }

        binding.closeFilter.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setDimAmount(0.5f)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
