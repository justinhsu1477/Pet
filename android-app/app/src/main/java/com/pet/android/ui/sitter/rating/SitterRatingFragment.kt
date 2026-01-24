package com.pet.android.ui.sitter.rating

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.R
import com.pet.android.data.model.SitterRatingStatsResponse
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.FragmentSitterRatingBinding
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SitterRatingFragment : Fragment() {

    private var _binding: FragmentSitterRatingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SitterRatingViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    private val adapter = SitterRatingAdapter()
    private var sitterId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSitterRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSitterId()
        observeStates()
    }

    private fun setupRecyclerView() {
        binding.rvRatings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRatings.adapter = adapter
    }

    private fun loadSitterId() {
        lifecycleScope.launch {
            sitterId = userPreferences.userId.first()
            val username = userPreferences.username.first()
            val roleName = userPreferences.roleName.first()

            Log.d(TAG, "loadSitterId - userId: $sitterId, username: $username")

            // 顯示 sitter 名字
            val displayName = roleName ?: username ?: "保母"
            binding.tvTitle.text = "$displayName 的評價"

            sitterId?.let { viewModel.load(it) } ?: run {
                showError("找不到保母 ID")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeStates() {
        viewModel.statsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    // no-op
                }
                is Resource.Success -> {
                    updateStatsUI(state.data)
                }
                is Resource.Error -> {
                    showError(state.message)
                }
            }
        }

        viewModel.listState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = state.data
                    if (data.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvRatings.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvRatings.visibility = View.VISIBLE
                        adapter.submitList(data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showError(state.message)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatsUI(stats: SitterRatingStatsResponse) {
        // Average rating
        binding.tvAverageRating.text = "%.1f".format(stats.average)
        binding.ratingBarAverage.rating = stats.average.toFloat()

        // Total reviews
        binding.tvTotalReviews.text = getString(R.string.reviews_count, stats.count.toInt())

        // Star breakdown
        val total = stats.count.coerceAtLeast(1)

        binding.progressStar5.progress = ((stats.star5 * 100) / total).toInt()
        binding.tvStar5Count.text = stats.star5.toString()

        binding.progressStar4.progress = ((stats.star4 * 100) / total).toInt()
        binding.tvStar4Count.text = stats.star4.toString()

        binding.progressStar3.progress = ((stats.star3 * 100) / total).toInt()
        binding.tvStar3Count.text = stats.star3.toString()

        binding.progressStar2.progress = ((stats.star2 * 100) / total).toInt()
        binding.tvStar2Count.text = stats.star2.toString()

        binding.progressStar1.progress = ((stats.star1 * 100) / total).toInt()
        binding.tvStar1Count.text = stats.star1.toString()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SitterRatingFragment"
    }
}
