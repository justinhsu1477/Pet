package com.pet.android.ui.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    val binding: VB by lazy {
        getViewBinding()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 設定返回按鈕行為與 UI 返回按鈕一致
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackButtonPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    abstract fun getViewBinding(): VB

    /**
     * 設定 Toolbar 並顯示返回按鈕
     *
     * @param toolbar MaterialToolbar 實例
     * @param title 標題文字（可選）
     * @param showBackButton 是否顯示返回按鈕，預設為 true
     */
    protected fun setupToolbar(
        toolbar: MaterialToolbar,
        title: String? = null,
        showBackButton: Boolean = true
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(title != null)
            title?.let { setTitle(it) }
            setDisplayHomeAsUpEnabled(showBackButton)
            setDisplayShowHomeEnabled(showBackButton)
        }

        if (showBackButton) {
            toolbar.setNavigationOnClickListener {
                onBackButtonPressed()
            }
        }
    }

    /**
     * 僅設定 Toolbar 的返回按鈕點擊事件（不使用 ActionBar）
     * 適用於已在 XML 中設定好 navigationIcon 的情況
     *
     * @param toolbar MaterialToolbar 實例
     */
    protected fun setupToolbarNavigation(toolbar: MaterialToolbar) {
        toolbar.setNavigationOnClickListener {
            onBackButtonPressed()
        }
    }

    /**
     * 處理返回按鈕點擊事件
     * 子類別可以覆寫此方法來自定義返回行為
     * 預設行為是直接結束 Activity
     */
    protected open fun onBackButtonPressed() {
        finish()
    }
}
