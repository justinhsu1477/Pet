package com.pet.android.di

import com.pet.android.data.api.AuthApi
import com.pet.android.data.api.BookingApi
import com.pet.android.data.api.CatApi
import com.pet.android.data.api.DogApi
import com.pet.android.data.api.PetApi
import com.pet.android.data.api.PetActivityApi
import com.pet.android.data.api.SitterApi
import com.pet.android.data.api.SitterBookingApi
import com.pet.android.data.api.SitterRatingApi
import com.pet.android.data.interceptor.AuthInterceptor
import com.pet.android.data.interceptor.TokenAuthenticator
import com.pet.android.data.preferences.EnvironmentManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url.host] ?: emptyList()
    }

    private fun logging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    @BaseOkHttp
    fun provideBaseOkHttpClient(
        cookieJar: CookieJar,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AuthedOkHttp
    fun provideAuthedOkHttpClient(
        @BaseOkHttp base: OkHttpClient,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return base.newBuilder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        @BaseOkHttp okHttpClient: OkHttpClient,
        environmentManager: EnvironmentManager
    ): Retrofit {
        val baseUrl = runBlocking { environmentManager.currentEnvironment.first().baseUrl }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(
        @AuthedOkHttp okHttpClient: OkHttpClient,
        environmentManager: EnvironmentManager
    ): Retrofit {
        val baseUrl = runBlocking { environmentManager.currentEnvironment.first().baseUrl }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun providePetApi(@ApiRetrofit retrofit: Retrofit): PetApi =
        retrofit.create(PetApi::class.java)

    @Provides
    @Singleton
    fun provideSitterApi(@ApiRetrofit retrofit: Retrofit): SitterApi =
        retrofit.create(SitterApi::class.java)

    @Provides
    @Singleton
    fun provideCatApi(@ApiRetrofit retrofit: Retrofit): CatApi =
        retrofit.create(CatApi::class.java)

    @Provides
    @Singleton
    fun provideDogApi(@ApiRetrofit retrofit: Retrofit): DogApi =
        retrofit.create(DogApi::class.java)

    @Provides
    @Singleton
    fun providePetActivityApi(@ApiRetrofit retrofit: Retrofit): PetActivityApi =
        retrofit.create(PetActivityApi::class.java)

    @Provides
    @Singleton
    fun provideSitterRatingApi(@ApiRetrofit retrofit: Retrofit): SitterRatingApi =
        retrofit.create(SitterRatingApi::class.java)

    @Provides
    @Singleton
    fun provideBookingApi(@ApiRetrofit retrofit: Retrofit): BookingApi =
        retrofit.create(BookingApi::class.java)

    @Provides
    @Singleton
    fun provideSitterBookingApi(@ApiRetrofit retrofit: Retrofit): SitterBookingApi =
        retrofit.create(SitterBookingApi::class.java)
}
