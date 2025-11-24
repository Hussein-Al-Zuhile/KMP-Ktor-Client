package com.hussein.ktorClient

import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlin.coroutines.CoroutineContext

data class HttpTypedResponse<Body>(
    override val call: HttpClientCall,
    override val status: HttpStatusCode,
    override val version: HttpProtocolVersion,
    override val requestTime: GMTDate,
    override val responseTime: GMTDate,
    override val headers: Headers,
    val body: Body,
    override val coroutineContext: CoroutineContext,
    @InternalAPI
    override val rawContent: ByteReadChannel
) : HttpResponse()