package com.pet.android.ui.sitter.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pet.android.data.repository.AuthRepository
import com.pet.android.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * SitterProfileViewModel 登出功能單元測試
 *
 * 測試範圍:
 * 1. 正常登出流程
 * 2. 登出時發生錯誤
 * 3. 登出不影響其他功能（如個人資料載入）
 */
@ExperimentalCoroutinesApi
class SitterProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SitterProfileViewModel

    @Mock
    private lateinit var authRepository: AuthRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = SitterProfileViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `logout should call authRepository logout`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
    }

    @Test
    fun `logout should handle repository success`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
    }

    @Test
    fun `logout should handle repository error gracefully`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(Resource.Error("Network error"))

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
        // 應該繼續執行，不拋出異常
    }

    @Test
    fun `logout should handle exception from repository`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenThrow(RuntimeException("Unexpected error"))

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
        // ViewModel 應該捕獲異常
    }

    @Test
    fun `logout should be callable multiple times`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.logout()
        advanceUntilIdle()
        viewModel.logout()
        advanceUntilIdle()
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(3)).logout()
    }

    @Test
    fun `logout should execute in viewModelScope`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.logout()

        // 在 coroutine 完成前不應該執行
        verify(authRepository, never()).logout()

        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
    }

    @Test
    fun `logout should not interfere with profile loading`() = runTest {
        // Arrange
        val sitterId = "test-sitter-id"
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.loadProfile(sitterId)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
        // loadProfile 應該正常執行（使用模擬資料）
    }

    @Test
    fun `logout should not affect profile state`() = runTest {
        // Arrange
        val sitterId = "test-sitter-id"
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act
        viewModel.loadProfile(sitterId)
        advanceUntilIdle()

        val profileStateBefore = viewModel.profileState.value

        viewModel.logout()
        advanceUntilIdle()

        val profileStateAfter = viewModel.profileState.value

        // Assert
        // profileState 應該保持不變
        assert(profileStateBefore == profileStateAfter) {
            "Profile state should not change after logout"
        }
    }

    @Test
    fun `logout should handle concurrent calls safely`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(Resource.Success(Unit))

        // Act - 同時呼叫多次登出
        viewModel.logout()
        viewModel.logout()
        viewModel.logout()

        advanceUntilIdle()

        // Assert - 應該被呼叫 3 次，不會衝突
        verify(authRepository, times(3)).logout()
    }

    @Test
    fun `logout should complete even with slow repository`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenAnswer {
            Thread.sleep(500) // 模擬慢速操作
            Resource.Success(Unit)
        }

        // Act
        viewModel.logout()

        // 立即檢查 - 不應該阻塞
        verify(authRepository, never()).logout()

        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
    }

    @Test
    fun `logout should handle null response from repository`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenReturn(null)

        // Act & Assert - 不應該崩潰
        try {
            viewModel.logout()
            advanceUntilIdle()
        } catch (e: Exception) {
            assert(false) { "Should handle null response gracefully" }
        }
    }

    @Test
    fun `logout should catch all exceptions`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenThrow(NullPointerException("Null pointer"))

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert - 應該捕獲所有類型的異常
        verify(authRepository, times(1)).logout()
    }
}
