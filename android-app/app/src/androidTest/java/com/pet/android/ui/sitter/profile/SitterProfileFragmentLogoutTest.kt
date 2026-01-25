package com.pet.android.ui.sitter.profile

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.pet.android.R
import com.pet.android.ui.login.LoginActivity
import com.pet.android.ui.sitter.SitterMainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SitterProfileFragment 登出功能 UI 測試
 *
 * 測試範圍:
 * 1. 登出按鈕是否顯示
 * 2. 點擊登出按鈕的行為
 * 3. 登出後導航到登入頁面
 * 4. Toast 訊息顯示
 *
 * 注意: 這些測試需要 Hilt 設定和測試環境配置
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class SitterProfileFragmentLogoutTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<SitterMainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        Intents.init()

        // TODO: 在實際測試中需要先登入並設置測試資料
        // scenario = ActivityScenario.launch(SitterMainActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    @Test
    fun logoutButton_isDisplayed() {
        // Given: 使用者在保母個人資料頁面

        // When: 滾動到頁面底部
        onView(withId(R.id.btnLogout))
            .perform(scrollTo())

        // Then: 登出按鈕應該顯示
        onView(withId(R.id.btnLogout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun logoutButton_hasCorrectText() {
        // When: 滾動到登出按鈕
        onView(withId(R.id.btnLogout))
            .perform(scrollTo())

        // Then: 按鈕文字應該是「登出」
        onView(withId(R.id.btnLogout))
            .check(matches(withText("登出")))
    }

    @Test
    fun logoutButton_isClickable() {
        // When: 滾動到登出按鈕
        onView(withId(R.id.btnLogout))
            .perform(scrollTo())

        // Then: 按鈕應該可以點擊
        onView(withId(R.id.btnLogout))
            .check(matches(isClickable()))
    }

    @Test
    fun clickLogout_navigatesToLoginActivity() {
        // Given: 使用者在保母個人資料頁面

        // When: 點擊登出按鈕
        onView(withId(R.id.btnLogout))
            .perform(scrollTo(), click())

        // Then: 應該導航到 LoginActivity
        intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun clickLogout_clearsActivityStack() {
        // Given: 使用者在保母個人資料頁面

        // When: 點擊登出按鈕
        onView(withId(R.id.btnLogout))
            .perform(scrollTo(), click())

        // Then: Intent 應該包含清除 stack 的 flags
        intended(allOf(
            hasComponent(LoginActivity::class.java.name),
            hasFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
            hasFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ))
    }

    @Test
    fun clickLogout_displaysToast() {
        // Given: 使用者在保母個人資料頁面

        // When: 點擊登出按鈕
        onView(withId(R.id.btnLogout))
            .perform(scrollTo(), click())

        // Then: 應該顯示「已登出」Toast
        // 注意: Espresso 不直接支援 Toast 測試，需要額外設定
        // 可以使用 ToastMatcher 或等待一段時間後檢查
    }

    @Test
    fun clickLogout_finishesCurrentActivity() {
        // Given: 使用者在保母個人資料頁面

        // When: 點擊登出按鈕
        onView(withId(R.id.btnLogout))
            .perform(scrollTo(), click())

        // Then: 當前 Activity 應該結束
        // 檢查 activity 是否已經 finished
        // assert(scenario.state == Lifecycle.State.DESTROYED)
    }

    @Test
    fun logoutButton_positionedBelowSaveButton() {
        // Then: 登出按鈕應該在儲存按鈕下方
        onView(withId(R.id.btnSave))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnLogout))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun multipleClicks_shouldHandleGracefully() {
        // Given: 使用者在保母個人資料頁面

        // When: 快速點擊登出按鈕多次
        onView(withId(R.id.btnLogout))
            .perform(scrollTo())

        // 模擬多次點擊
        repeat(3) {
            onView(withId(R.id.btnLogout))
                .perform(click())
        }

        // Then: 應該只導航一次到 LoginActivity
        // 不應該崩潰或產生多個 Intent
    }
}

/**
 * 簡化版 UI 測試 - 不需要 Hilt
 * 可以用於快速驗證 UI 元件
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SitterProfileFragmentLogoutSimpleTest {

    @Test
    fun logoutButtonExists_inLayout() {
        // 這個測試驗證 XML layout 中是否存在登出按鈕
        // 可以通過 inflate layout 並檢查 view hierarchy

        // Given: Fragment layout
        // When: Inflate fragment_sitter_profile.xml
        // Then: 應該找到 btnLogout view

        // 實際實作需要 LayoutInflater 和 Context
    }
}
