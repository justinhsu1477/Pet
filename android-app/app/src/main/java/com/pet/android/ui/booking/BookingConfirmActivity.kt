package com.pet.android.ui.booking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.pet.android.R
import com.pet.android.data.model.AvailableSitterResponse
import com.pet.android.data.model.BookingRequest
import com.pet.android.data.model.Pet
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.ActivityBookingConfirmBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class BookingConfirmActivity : BaseActivity<ActivityBookingConfirmBinding>() {

    private val viewModel: BookingConfirmViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    private lateinit var pet: Pet
    private lateinit var sitter: AvailableSitterResponse
    private lateinit var date: String
    private var durationHours: Int = 8

    override fun getViewBinding(): ActivityBookingConfirmBinding =
        ActivityBookingConfirmBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pet = intent.getSerializableExtra(EXTRA_PET) as Pet
        sitter = intent.getSerializableExtra(EXTRA_SITTER) as AvailableSitterResponse
        date = intent.getStringExtra(EXTRA_DATE) ?: ""
        durationHours = intent.getIntExtra(EXTRA_DURATION_HOURS, 8)

        setupToolbar()
        setupViews()
        setupSubmitButton()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupViews() {
        // Pet info
        binding.tvPetName.text = pet.name
        binding.tvPetType.text = pet.type

        // Set pet icon based on type
        val petIcon = if (pet.type.equals("DOG", ignoreCase = true)) {
            R.drawable.ic_dog
        } else {
            R.drawable.ic_cat
        }
        binding.ivPetIcon.setImageResource(petIcon)

        // Sitter info
        binding.tvSitterAvatar.text = sitter.name.firstOrNull()?.toString() ?: "?"
        binding.tvSitterName.text = sitter.name
        val rating = sitter.averageRating ?: 0.0
        val ratingCount = sitter.ratingCount ?: 0
        binding.tvSitterRating.text = String.format("%.1f (%d)", rating, ratingCount)

        // Time info
        binding.tvDate.text = date
        val durationText = when (durationHours) {
            4 -> getString(R.string.booking_half_day)
            24 -> getString(R.string.booking_overnight)
            else -> getString(R.string.booking_full_day)
        }
        binding.tvDuration.text = durationText
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            submitBooking()
        }
    }

    private fun submitBooking() {
        val userId = runBlocking { userPreferences.userId.first() }
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate start and end time
        val localDate = LocalDate.parse(date)
        val startTime = LocalDateTime.of(localDate, java.time.LocalTime.of(9, 0))
        val endTime = startTime.plusHours(durationHours.toLong())

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val request = BookingRequest(
            petId = pet.id ?: "",
            sitterId = sitter.id,
            startTime = startTime.format(formatter),
            endTime = endTime.format(formatter),
            notes = binding.etNotes.text?.toString()?.takeIf { it.isNotBlank() }
        )

        viewModel.createBooking(userId, request)
    }

    private fun observeState() {
        viewModel.bookingState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.booking_success), Toast.LENGTH_SHORT).show()
                    // Go back to home or show success
                    setResult(RESULT_OK)
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val EXTRA_PET = "extra_pet"
        private const val EXTRA_SITTER = "extra_sitter"
        private const val EXTRA_DATE = "extra_date"
        private const val EXTRA_DURATION_HOURS = "extra_duration_hours"

        fun start(
            context: Context,
            pet: Pet,
            sitter: AvailableSitterResponse,
            date: String,
            durationHours: Int
        ) {
            val intent = Intent(context, BookingConfirmActivity::class.java).apply {
                putExtra(EXTRA_PET, pet as Serializable)
                putExtra(EXTRA_SITTER, sitter as Serializable)
                putExtra(EXTRA_DATE, date)
                putExtra(EXTRA_DURATION_HOURS, durationHours)
            }
            context.startActivity(intent)
        }
    }
}
