package com.hussein.ktorClient

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


fun defaultJsonHttpClient(
    baseUrl: String,
    isLoggingEnabled: Boolean,
    accessToken: String? = null,
    block: HttpClientConfig<*>.() -> Unit,
) = HttpClient(CIO) {
    // This will throw exception if response status is not successful (>= 3xx)
    expectSuccess = true

    install(Resources)

    engine {
        requestTimeout = 20_000
    }

    if (isLoggingEnabled) {
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    defaultRequest {
        url(baseUrl)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        accessToken?.let { header(HttpHeaders.Authorization, it) }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            },

            )
    }
    block()
}