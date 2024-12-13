package com.teamfour.kooksy.ui.home

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.teamfour.kooksy.R
import com.teamfour.kooksy.databinding.FragmentRecipeBinding
import com.teamfour.kooksy.ui.favorite.FavoritesViewModel
import com.teamfour.kooksy.ui.profile.Recipe
import com.teamfour.kooksy.ui.profile.UserDetails

class FragmentRecipeDetails : Fragment() {
    private lateinit var recipeItem: Recipe
    private var isFav: Boolean = false
    private var isIconToggled: Boolean = false
    val viewmodel: FavoritesViewModel by viewModels()
    private lateinit var binding: FragmentRecipeBinding
    val args: FragmentRecipeDetailsArgs by navArgs()
    var ratedBy = emptyList<String>()
    var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recipeItem = args.recipeItem
        ratedBy = recipeItem.ratedBy
        userId = UserDetails.user?.user_id

        (requireActivity() as AppCompatActivity).supportActionBar?.title = recipeItem.recipe_name
        setEmojiDrawable()
        isFav = recipeItem.is_favourite
        setRecyclerViewData()
        setupTabListener()
        setOptionsMenu(isFav)
        observeViewModel()
    }

    private fun setupTabListener() {
        binding.recipeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> binding.recipeDetailsRecyclerView.adapter =
                        RecipeDetailsAdapter(recipeItem.recipe_ingredients)

                    1 -> binding.recipeDetailsRecyclerView.adapter =
                        ProcedureAdapter(recipeItem.recipe_instructions)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setRecyclerViewData() {
        val adapter = RecipeDetailsAdapter(recipeItem.recipe_ingredients)
        val spaceDecoration = SpaceItemDecoration(16)
        binding.recipeDetailsRecyclerView.addItemDecoration(spaceDecoration)
        binding.recipeDetailsRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewmodel.isFavouritevalueUpdated.observe(viewLifecycleOwner) { added ->
            if (added) {
                Toast.makeText(
                    requireActivity(),
                    "Added to favourites successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Removed from favourites successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewmodel.isRatingvalueUpdated.observe(viewLifecycleOwner) { added ->
            if (added) {
                playSuccessAnimation()
                binding.ratingBtn.isEnabled = false
                binding.recipeRatingBar.rating = recipeItem.averageRating.toFloat()
                Toast.makeText(
                    requireActivity(),
                    "Thank you for the feedback!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun playSuccessAnimation() {
        val ratingAnimation = binding.ratingAnimation
        ratingAnimation.visibility = View.VISIBLE
        ratingAnimation.playAnimation()

        Log.d("FragmentRecipeDetails", "Playing rating success animation.")

        ratingAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Log.d("FragmentRecipeDetails", "Animation started.")
            }

            override fun onAnimationEnd(animation: Animator) {
                Log.d("FragmentRecipeDetails", "Animation ended.")
                ratingAnimation.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                Log.d("FragmentRecipeDetails", "Animation canceled.")
            }

            override fun onAnimationRepeat(animation: Animator) {
                Log.d("FragmentRecipeDetails", "Animation repeated.")
            }
        })
    }

    private fun setEmojiDrawable() {
        val difficultyLevel = recipeItem.recipe_difficultyLevel
        val drawable = DifficultyLevel.valueOf(difficultyLevel).drawableRes
        val emojiDrawable = ResourcesCompat.getDrawable(resources, drawable, null)

        if (!recipeItem.recipe_imageURL.isNullOrEmpty()) {
            Picasso.get()
                .load(recipeItem.recipe_imageURL)
                .placeholder(R.drawable.recipe_load) // Placeholder image
                .error(R.drawable.recipe_error) // Error image
                .into(binding.recipePageImage)
        } else {
            // Set a placeholder image if URL is empty
            binding.recipePageImage.setImageResource(R.drawable.recipe_load)
        }
        binding.cookingTime.text = recipeItem.recipe_cookTime.toString().plus(" mins")
        binding.recipeDifficultyLevel.setCompoundDrawablesWithIntrinsicBounds(
            null,
            emojiDrawable,
            null,
            null
        )
        binding.recipeCalories.text = recipeItem.recipe_calories.toString().plus(" Cals")
        Log.d("RatingBar", "Average Rating: ${recipeItem.averageRating}")
        binding.recipeRatingBar.rating = recipeItem.averageRating.toFloat()
        binding.recipeRatingBar.isClickable = false

        binding.ratingBtn.isEnabled = /*!recipeItem.is_rated &&*/ !ratedBy.contains(userId)
        //alpha - Adjusts the transparency
        binding.ratingBtn.alpha = if (ratedBy.contains(userId)) 0.5f else 1.0f
        binding.ratingBtn.setOnClickListener { showRatingDialog() }
    }

    private fun setOptionsMenu(isFav: Boolean) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fav_menu, menu)
                val item = menu.findItem(R.id.fav_icon)
                isIconToggled = isFav
                item.icon = if (isFav) {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_fav_selected, null)
                } else {
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_fav_unselected, null)
                }
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.fav_icon -> {
                        toggleMenuItemIcon(item)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun toggleMenuItemIcon(item: MenuItem) {
        item.icon = if (isIconToggled) {
            ResourcesCompat.getDrawable(resources, R.drawable.ic_fav_unselected, null)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.ic_fav_selected, null)
        }
        isIconToggled = !isIconToggled
        viewmodel.updateFavoriteStatus(isIconToggled, args.recipeItem)
    }

    private fun showRatingDialog() {
        val dialogView =
            LayoutInflater.from(requireActivity()).inflate(R.layout.rating_dialog, null)
        val dialog = AlertDialog.Builder(requireActivity(), R.style.EmotionRatingTheme)
            .setTitle("Rate your experience")
            .setView(dialogView)
            .create()

        val scaleUp = AnimationUtils.loadAnimation(requireActivity(), R.anim.scale_up)

        var ratingValue = 0

        dialogView.findViewById<AppCompatImageButton>(R.id.rating_one).setOnClickListener {
            ratingValue = 1
            it.startAnimation(scaleUp)
        }
        dialogView.findViewById<AppCompatImageButton>(R.id.rating_two).setOnClickListener {
            ratingValue = 2
            it.startAnimation(scaleUp)
        }
        dialogView.findViewById<AppCompatImageButton>(R.id.rating_three).setOnClickListener {
            ratingValue = 3
            it.startAnimation(scaleUp)
        }
        dialogView.findViewById<AppCompatImageButton>(R.id.rating_four).setOnClickListener {
            ratingValue = 4
            it.startAnimation(scaleUp)
        }
        dialogView.findViewById<AppCompatImageButton>(R.id.rating_five).setOnClickListener {
            ratingValue = 5
            it.startAnimation(scaleUp)
        }

        dialogView.findViewById<Button>(R.id.submitRating).setOnClickListener {
            if (ratingValue > 0) {
                // Checks if the current user is not in the list of users who have already rated the recipe (ratedBy).
                val isCurrentUserNotRated = ratedBy.contains(userId).not()
                if (isCurrentUserNotRated){
                    // recipeItem.is_rated = isCurrentUserNotRated
                    viewmodel.submitRating(isCurrentUserNotRated, ratingValue, recipeItem)
                }else {
                    Toast.makeText(requireActivity(), "You have already rated this recipe", Toast.LENGTH_SHORT).show()
                }
            } else Toast.makeText(requireActivity(), "Please select rating", Toast.LENGTH_SHORT)
                .show()

            dialog.dismiss()
        }

        dialog.show()
    }

    enum class DifficultyLevel(val drawableRes: Int) {
        Easy(R.drawable.hearteyes_emoji), Medium(R.drawable.happy_emoji), Hard(R.drawable.grinningface_emoji)
    }
}