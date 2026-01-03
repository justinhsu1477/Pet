package com.pet.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.util.TimeZone

@HiltAndroidApp
class PetCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 設定全域預設時區為台灣時區
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"))
    }
}

