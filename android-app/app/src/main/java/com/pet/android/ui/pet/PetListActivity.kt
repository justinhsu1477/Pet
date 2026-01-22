package com.pet.android.ui.pet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.R
import com.pet.android.data.model.Pet
import com.pet.android.databinding.ActivityPetListBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.create.CreatePetActivity
import com.pet.android.ui.sitter.SitterListActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PetListActivity : BaseActivity<ActivityPetListBinding>() {

    private val viewModel: PetViewModel by viewModels()
    private lateinit var petAdapter: PetAdapter

    private val createPetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh the list when a new pet is created
            viewModel.loadPets()
        }
    }

    private val editPetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh the list when a pet is updated
            viewModel.loadPets()
        }
    }

    override fun getViewBinding() = ActivityPetListBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView()
        setupViews()
        setupFilterChips()
        observePetsState()
        viewModel.loadPets()
    }

    private fun setupRecyclerView() {
        petAdapter = PetAdapter(
            onItemClick = { pet ->
                // Handle pet item click - show activity quick dialog
                showActivityQuickDialog(pet)
            },
            onItemLongClick = { pet ->
                // Handle pet item long click - go to edit page
                openEditPetActivity(pet)
            }
        )
        binding.rvPets.apply {
            layoutManager = LinearLayoutManager(this@PetListActivity)
            adapter = petAdapter
        }
    }

    private fun showActivityQuickDialog(pet: Pet) {
        val dialog = ActivityQuickDialog.newInstance(pet)
        dialog.show(supportFragmentManager, ActivityQuickDialog.TAG)
    }

    private fun openEditPetActivity(pet: Pet) {
        val intent = Intent(this, EditPetActivity::class.java).apply {
            putExtra(EditPetActivity.EXTRA_PET_ID, pet.id)
            putExtra(EditPetActivity.EXTRA_PET_TYPE, pet.type)
        }
        editPetLauncher.launch(intent)
    }

    private fun setupViews() {
        setupToolbar(binding.toolbar,"管理員 寵物清單",false)

        binding.btnToSitters.setOnClickListener {
            startActivity(Intent(this, SitterListActivity::class.java))
        }

        binding.fabAdd.setOnClickListener {
            createPetLauncher.launch(Intent(this, CreatePetActivity::class.java))
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val filter = when (checkedIds.first()) {
                    R.id.chipDog -> PetFilter.DOG
                    R.id.chipCat -> PetFilter.CAT
                    else -> PetFilter.ALL
                }
                viewModel.setFilter(filter)
            }
        }
    }

    private fun observePetsState() {
        viewModel.petsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPets.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val pets = resource.data
                    if (pets.isEmpty()) {
                        binding.rvPets.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                        updateEmptyStateMessage()
                    } else {
                        binding.rvPets.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        petAdapter.submitList(pets)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = resource.message
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateEmptyStateMessage() {
        val filter = viewModel.currentFilter.value ?: PetFilter.ALL
        val message = when (filter) {
            PetFilter.ALL -> getString(R.string.no_pets_found)
            PetFilter.DOG -> getString(R.string.no_dogs_found)
            PetFilter.CAT -> getString(R.string.no_cats_found)
        }
        binding.tvEmptyMessage.text = message
    }

    override fun onBackButtonPressed() {
        finishAffinity()
    }
}
