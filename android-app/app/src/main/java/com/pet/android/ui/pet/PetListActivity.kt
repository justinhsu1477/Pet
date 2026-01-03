package com.pet.android.ui.pet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pet.android.databinding.ActivityPetListBinding
import com.pet.android.ui.base.BaseActivity
import com.pet.android.ui.sitter.SitterListActivity
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PetListActivity : BaseActivity<ActivityPetListBinding>() {

    private val viewModel: PetViewModel by viewModels()
    private lateinit var petAdapter: PetAdapter

    override fun getViewBinding() = ActivityPetListBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView()
        setupViews()
        observePetsState()
        viewModel.loadPets()
    }

    private fun setupRecyclerView() {
        petAdapter = PetAdapter { pet ->
            // Handle pet item click
            Toast.makeText(this, "點擊: ${pet.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvPets.apply {
            layoutManager = LinearLayoutManager(this@PetListActivity)
            adapter = petAdapter
        }
    }

    private fun setupViews() {
        binding.btnToSitters.setOnClickListener {
            startActivity(Intent(this, SitterListActivity::class.java))
        }

        binding.fabAdd.setOnClickListener {
            Toast.makeText(this, "新增寵物功能待實作", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observePetsState() {
        viewModel.petsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPets.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvPets.visibility = View.VISIBLE
                    petAdapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
