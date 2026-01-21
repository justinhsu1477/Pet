package com.pet.shared.network

import io.ktor.client.HttpClient

// Expect platform-specific HttpClient builders with baseUrl configuration
expect fun createHttpClient(baseUrl: String): HttpClient
