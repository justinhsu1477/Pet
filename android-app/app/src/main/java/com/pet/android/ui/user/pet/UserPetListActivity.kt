package com.pet.android.ui.user.pet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pet.android.R
import com.pet.android.data.model.Pet
import com.pet.android.databinding.ActivityUserPetListBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.create.CreatePetActivity
import com.pet.android.ui.pet.EditPetActivity
import com.pet.android.ui.pet.PetAdapter
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserPetListActivity : BaseActivity<ActivityUserPetListBinding>() {

    private val viewModel: UserPetViewModel by viewModels()
    private lateinit var petAdapter: PetAdapter

    private val createPetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadUserPets()
            setResult(RESULT_OK)
        }
    }

    private val editPetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadUserPets()
            setResult(RESULT_OK)
        }
    }

    override fun getViewBinding() = ActivityUserPetListBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeStates()
        viewModel.loadUserPets()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        petAdapter = PetAdapter(
            onItemClick = { pet ->
                // Navigate to edit pet
                openEditPetActivity(pet)
            },
            onItemLongClick = { pet ->
                // Show delete confirmation dialog
                showDeleteConfirmDialog(pet)
            }
        )
        binding.rvPets.apply {
            layoutManager = LinearLayoutManager(this@UserPetListActivity)
            adapter = petAdapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            createPetLauncher.launch(Intent(this, CreatePetActivity::class.java))
        }
    }

    private fun openEditPetActivity(pet: Pet) {
        val petId = pet.id ?: return
        val intent = Intent(this, EditPetActivity::class.java).apply {
            putExtra(EditPetActivity.EXTRA_PET_ID, petId)
            putExtra(EditPetActivity.EXTRA_PET_TYPE, pet.type)
        }
        editPetLauncher.launch(intent)
    }

    private fun showDeleteConfirmDialog(pet: Pet) {
        val petId = pet.id ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_pet_title)
            .setMessage(R.string.confirm_delete_pet)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePet(petId)
            }
            .show()
    }

    private fun observeStates() {
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
                    } else {
                        binding.rvPets.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        petAdapter.submitList(pets)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deleteState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, R.string.pet_deleted, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
