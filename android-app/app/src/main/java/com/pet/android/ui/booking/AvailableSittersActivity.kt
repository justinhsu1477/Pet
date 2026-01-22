package com.pet.android.ui.booking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.R
import com.pet.android.data.model.AvailableSitterResponse
import com.pet.android.data.model.Pet
import com.pet.android.databinding.ActivityAvailableSittersBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.sitter.rating.SitterRatingActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class AvailableSittersActivity : BaseActivity<ActivityAvailableSittersBinding>() {

    private lateinit var adapter: AvailableSitterAdapter
    private lateinit var pet: Pet
    private lateinit var date: String
    private var durationHours: Int = 8
    private var sitters: List<AvailableSitterResponse> = emptyList()

    override fun getViewBinding(): ActivityAvailableSittersBinding =
        ActivityAvailableSittersBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent
        pet = intent.getSerializableExtra(EXTRA_PET) as Pet
        date = intent.getStringExtra(EXTRA_DATE) ?: ""
        durationHours = intent.getIntExtra(EXTRA_DURATION_HOURS, 8)

        @Suppress("UNCHECKED_CAST")
        sitters = intent.getSerializableExtra(EXTRA_SITTERS) as? ArrayList<AvailableSitterResponse> ?: emptyList()

        setupToolbar(binding.toolbar,"預約保母服務",true)
        setupInfo()
        setupRecyclerView()
        displaySitters()
    }


    private fun setupInfo() {
        binding.tvSitterCount.text = getString(R.string.booking_sitter_count, sitters.size)

        val durationText = when (durationHours) {
            4 -> getString(R.string.booking_half_day)
            24 -> getString(R.string.booking_overnight)
            else -> getString(R.string.booking_full_day)
        }
        binding.tvBookingInfo.text = "$date | $durationText | ${pet.name}"
    }

    private fun setupRecyclerView() {
        adapter = AvailableSitterAdapter(
            onViewRatings = { sitter ->
                SitterRatingActivity.start(this, sitter.id, sitter.name)
            },
            onSelect = { sitter ->
                BookingConfirmActivity.start(this, pet, sitter, date, durationHours)
            }
        )
        binding.rvSitters.layoutManager = LinearLayoutManager(this)
        binding.rvSitters.adapter = adapter
    }

    private fun displaySitters() {
        if (sitters.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvSitters.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvSitters.visibility = View.VISIBLE
            adapter.submitList(sitters)
        }
    }

    companion object {
        private const val EXTRA_PET = "extra_pet"
        private const val EXTRA_DATE = "extra_date"
        private const val EXTRA_DURATION_HOURS = "extra_duration_hours"
        private const val EXTRA_SITTERS = "extra_sitters"

        fun start(
            context: Context,
            pet: Pet,
            date: String,
            durationHours: Int,
            sitters: ArrayList<AvailableSitterResponse>
        ) {
            val intent = Intent(context, AvailableSittersActivity::class.java).apply {
                putExtra(EXTRA_PET, pet as Serializable)
                putExtra(EXTRA_DATE, date)
                putExtra(EXTRA_DURATION_HOURS, durationHours)
                putExtra(EXTRA_SITTERS, sitters)
            }
            context.startActivity(intent)
        }
    }
}
