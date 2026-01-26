package com.pet.android.ui.booking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import com.pet.android.R
import com.pet.android.data.model.Pet
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.ActivityBookingHomeBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.user.pet.UserPetListActivity
import com.pet.android.util.LogoutHelper
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BookingHomeActivity : BaseActivity<ActivityBookingHomeBinding>() {

    private val viewModel: BookingHomeViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    @Inject
    lateinit var logoutHelper: LogoutHelper

    private var pets: List<Pet> = emptyList()

    private val myPetsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh pets list when returning from pet management
            loadUserPets()
        }
    }

    override fun getViewBinding(): ActivityBookingHomeBinding =
        ActivityBookingHomeBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupCalendar()
        setupTimeChips()
        setupSearchButton()
        observeStates()

        loadUserPets()
    }

    private fun setupToolbar() {
        setupToolbar(binding.toolbar, getString(R.string.booking_title), false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_my_pets -> {
                myPetsLauncher.launch(Intent(this, UserPetListActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logoutHelper.performLogout(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackButtonPressed() {
        finishAffinity()
    }

    private fun setupCalendar() {
        // Set minimum date to today
        binding.calendarView.minDate = System.currentTimeMillis()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            updateSelectedDateDisplay()
        }
    }

    private fun updateSelectedDateDisplay() {
        viewModel.selectedDate?.let { date ->
            val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 (E)", Locale.TAIWAN)
            binding.tvSelectedDate.text = date.format(formatter)
        }
    }

    private fun setupTimeChips() {
        binding.chipGroupTime.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                viewModel.selectedDuration = when (checkedIds[0]) {
                    R.id.chipHalfDay -> BookingHomeViewModel.Duration.HALF_DAY
                    R.id.chipFullDay -> BookingHomeViewModel.Duration.FULL_DAY
                    R.id.chipOvernight -> BookingHomeViewModel.Duration.OVERNIGHT
                    else -> BookingHomeViewModel.Duration.FULL_DAY
                }
            }
        }
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            when {
                viewModel.selectedPet == null -> {
                    Toast.makeText(this, "請選擇寵物", Toast.LENGTH_SHORT).show()
                }
                viewModel.selectedDate == null -> {
                    Toast.makeText(this, "請選擇日期", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    viewModel.searchAvailableSitters()
                }
            }
        }
    }

    private fun loadUserPets() {
        lifecycleScope.launch {
            val userId = userPreferences.userId.first()
            if (userId != null) {
                viewModel.loadUserPets(userId)
            } else {
                Toast.makeText(this@BookingHomeActivity, "請先登入", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun observeStates() {
        viewModel.petsState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    pets = state.data
                    setupPetSpinner(pets)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.sittersState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSearch.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSearch.isEnabled = true

                    val sitters = state.data
                    if (sitters.isEmpty()) {
                        Toast.makeText(this, getString(R.string.booking_no_sitters), Toast.LENGTH_SHORT).show()
                    } else {
                        // Navigate to available sitters screen
                        AvailableSittersActivity.start(
                            this,
                            viewModel.selectedPet!!,
                            viewModel.selectedDate!!.toString(),
                            viewModel.selectedDuration.hours,
                            ArrayList(sitters)
                        )
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSearch.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupPetSpinner(pets: List<Pet>) {
        if (pets.isEmpty()) {
            binding.spinnerPet.visibility = View.GONE
            binding.tvNoPets.visibility = View.VISIBLE
            binding.btnSearch.isEnabled = false
            return
        }

        binding.spinnerPet.visibility = View.VISIBLE
        binding.tvNoPets.visibility = View.GONE
        binding.btnSearch.isEnabled = true

        val petNames = pets.map { "${it.name} (${it.breed})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPet.adapter = adapter

        binding.spinnerPet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedPet = pets[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.selectedPet = null
            }
        }

        // Select first pet by default
        if (pets.isNotEmpty()) {
            viewModel.selectedPet = pets[0]
        }
    }

    companion object {
        private const val TAG = "BookingHomeActivity"

        fun start(context: Context) {
            context.startActivity(Intent(context, BookingHomeActivity::class.java))
        }
    }
}
