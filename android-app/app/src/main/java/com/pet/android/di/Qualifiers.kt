package com.pet.android.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedOkHttp
