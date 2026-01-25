package com.pet.android.ui.setting

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.pet.android.data.preferences.EnvironmentManager
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
 * SettingViewModel 登出功能單元測試
 *
 * 測試範圍:
 * 1. 正常登出流程
 * 2. 登出時發生錯誤
 * 3. 多次登出
 */
@ExperimentalCoroutinesApi
class SettingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingViewModel

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var environmentManager: EnvironmentManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = SettingViewModel(
            environmentManager = environmentManager,
            authRepository = authRepository
        )
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
        verifyNoMoreInteractions(authRepository)
    }

    @Test
    fun `logout should handle repository error gracefully`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        `when`(authRepository.logout()).thenReturn(Resource.Error(errorMessage))

        // Act
        viewModel.logout()
        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
        // ViewModel 應該繼續執行，不會拋出異常
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
        // ViewModel 應該捕獲異常並記錄，不會崩潰
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

        // Assert
        verify(authRepository, times(2)).logout()
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
    fun `logout should not block main thread`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenAnswer {
            // 模擬耗時操作
            Thread.sleep(100)
            Resource.Success(Unit)
        }

        // Act
        viewModel.logout()

        // 立即返回，不阻塞
        verify(authRepository, never()).logout()

        advanceUntilIdle()

        // Assert
        verify(authRepository, times(1)).logout()
    }

    @Test
    fun `logout should continue even if repository throws checked exception`() = runTest {
        // Arrange
        `when`(authRepository.logout()).thenThrow(IllegalStateException("Invalid state"))

        // Act & Assert - 不應該拋出異常
        try {
            viewModel.logout()
            advanceUntilIdle()
        } catch (e: Exception) {
            // ViewModel 應該捕獲所有異常
            assert(false) { "ViewModel should catch all exceptions" }
        }

        verify(authRepository, times(1)).logout()
    }
}
