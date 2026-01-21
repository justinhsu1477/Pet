package com.pet.android.ui.create

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.pet.android.databinding.ActivityCreatePetBinding
import com.pet.android.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePetActivity : BaseActivity<ActivityCreatePetBinding>() {

    private val createPetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun getViewBinding() = ActivityCreatePetBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbarNavigation(binding.toolbar)
        setupClicks()
    }

    private fun setupClicks() {
        binding.cardDog.setOnClickListener {
            onSpeciesSelected(Species.DOG)
        }
        binding.cardCat.setOnClickListener {
            onSpeciesSelected(Species.CAT)
        }
    }

    private fun onSpeciesSelected(species: Species) {
        val intent = Intent(this, CreatePetFormActivity::class.java).apply {
            putExtra(CreatePetFormActivity.EXTRA_SPECIES, species.name)
        }
        createPetLauncher.launch(intent)
    }
}

enum class Species { DOG, CAT }
