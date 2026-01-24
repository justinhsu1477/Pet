package com.pet.android.ui.sitter.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.pet.android.data.preferences.UserPreferencesManager
import com.pet.android.databinding.FragmentSitterProfileBinding
import com.pet.android.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 保母個人檔案頁面
 * 根據 SITTER_FEATURE_DESIGN.md 設計
 *
 * 功能:
 * - 個人照片上傳
 * - 自我介紹編輯
 * - 服務項目勾選
 * - 可服務時段設置
 * - 服務區域選擇
 * - 收費標準編輯
 */
@AndroidEntryPoint
class SitterProfileFragment : Fragment() {

    private var _binding: FragmentSitterProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SitterProfileViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferencesManager

    private var sitterId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSitterProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        loadSitterId()
        observeProfile()
    }

    private fun setupViews() {
        // 保存按鈕
        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        // 編輯照片按鈕 (暫時顯示 Toast)
        binding.ivProfilePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "照片上傳功能開發中", Toast.LENGTH_SHORT).show()
        }

        // 服務項目 CheckBoxes
        setupServiceItems()
    }

    private fun setupServiceItems() {
        // 根據設計文檔,常見服務項目包括:
        // - 遛狗
        // - 餵食
        // - 陪玩
        // - 寵物美容
        // - 寵物訓練
        // 這些項目在布局檔案中設置
    }

    private fun loadSitterId() {
        lifecycleScope.launch {
            sitterId = userPreferences.userId.first()
            val username = userPreferences.username.first()
            val roleName = userPreferences.roleName.first()

            Log.d(TAG, "loadSitterId - userId: $sitterId, username: $username")

            // 顯示保母名稱
            val displayName = roleName ?: username ?: "保母"
            binding.tvSitterName.text = displayName

            // 載入保母資料
            sitterId?.let {
                viewModel.loadProfile(it)
            } ?: run {
                Toast.makeText(requireContext(), "無法取得保母 ID，請重新登入", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeProfile() {
        viewModel.profileState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                    resource.data?.let { profile ->
                        displayProfile(profile)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "儲存中..."
                }
                is Resource.Success -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "儲存"
                    Toast.makeText(requireContext(), "個人檔案已更新", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "儲存"
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayProfile(profile: SitterProfileData) {
        // 個人資訊
        binding.etIntroduction.setText(profile.introduction)
        binding.etExperience.setText(profile.experience)
        binding.etServiceArea.setText(profile.serviceArea)
        binding.etPricing.setText(profile.pricing)
        binding.etAvailableTime.setText(profile.availableTime)
        binding.etCertifications.setText(profile.certifications)

        // 服務項目 (假設 profile 有服務項目列表)
        binding.cbWalking.isChecked = profile.services.contains("遛狗")
        binding.cbFeeding.isChecked = profile.services.contains("餵食")
        binding.cbPlaying.isChecked = profile.services.contains("陪玩")
        binding.cbGrooming.isChecked = profile.services.contains("美容")
        binding.cbTraining.isChecked = profile.services.contains("訓練")
    }

    private fun saveProfile() {
        sitterId?.let { id ->
            val introduction = binding.etIntroduction.text.toString()
            val experience = binding.etExperience.text.toString()
            val serviceArea = binding.etServiceArea.text.toString()
            val pricing = binding.etPricing.text.toString()
            val availableTime = binding.etAvailableTime.text.toString()
            val certifications = binding.etCertifications.text.toString()

            // 收集勾選的服務項目
            val services = mutableListOf<String>()
            if (binding.cbWalking.isChecked) services.add("遛狗")
            if (binding.cbFeeding.isChecked) services.add("餵食")
            if (binding.cbPlaying.isChecked) services.add("陪玩")
            if (binding.cbGrooming.isChecked) services.add("美容")
            if (binding.cbTraining.isChecked) services.add("訓練")

            val profileData = SitterProfileData(
                introduction = introduction,
                experience = experience,
                serviceArea = serviceArea,
                pricing = pricing,
                availableTime = availableTime,
                certifications = certifications,
                services = services
            )

            viewModel.saveProfile(id, profileData)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SitterProfileFragment"
    }
}

/**
 * 保母個人檔案資料模型
 * 暫時定義,實際應該從後端 API 獲取
 */
data class SitterProfileData(
    val introduction: String = "",
    val experience: String = "",
    val serviceArea: String = "",
    val pricing: String = "",
    val availableTime: String = "",
    val certifications: String = "",
    val services: List<String> = emptyList()
)
