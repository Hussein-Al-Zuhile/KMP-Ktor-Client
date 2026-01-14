@file:Suppress("unused", "RedundantWith")

package com.hussein.ktorClient

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.prepareGet
import io.ktor.client.plugins.resources.preparePost
import io.ktor.client.plugins.resources.preparePut
import io.ktor.client.plugins.resources.prepareRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.utils.io.InternalAPI

abstract class BaseHttpService(protected val client: HttpClient) {

    protected suspend inline fun <reified Resource : ApiResource> prepareGet(
        endPoint: ApiEndPoint<*, *, Resource>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement =
        client.prepareGet(endPoint.resource) {
            addRequestBodyIfExist(endPoint)
            builder()
        }

    protected suspend inline fun <reified Resource : ApiResource> preparePost(
        endPoint: ApiEndPoint<*, *, Resource>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePost(endPoint.resource) {
        addRequestBodyIfExist(endPoint)
        builder()
    }

    protected suspend inline fun <reified Resource : ApiResource> preparePut(
        endPoint: ApiEndPoint<*, *, Resource>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePut(endPoint.resource) {
        addRequestBodyIfExist(endPoint)
        builder()
    }

    protected suspend inline fun <reified Resource : ApiResource> prepareSubmitForm(
        endPoint: ApiEndPoint<*, *, Resource>,
        formParameters: Parameters,
        encodeInQuery: Boolean = false,
        method: HttpMethod? = null,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = client.prepareRequest(endPoint.resource) {
        addRequestBodyIfExist(endPoint)
        if (encodeInQuery) {
            this.method = method ?: HttpMethod.Get
            url.parameters.appendAll(formParameters)
        } else {
            this.method = method ?: HttpMethod.Post
            setBody(FormDataContent(formParameters))
        }
        builder()
    }

    protected suspend inline fun <reified Resource : ApiResource> prepareSubmitFormWithBinaryData(
        endPoint: ApiEndPoint<*, *, Resource>,
        formParameters: List<PartData>,
        encodeInQuery: Boolean = false,
        method: HttpMethod = HttpMethod.Post,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = client.prepareRequest(endPoint.resource) {
        this.method = method
        addRequestBodyIfExist(endPoint)
        setBody(MultiPartFormDataContent(formParameters))
        builder()
    }

    protected suspend inline fun <reified ResponseModel, reified Resource : ApiResource> get(
        endPoint: ApiEndPoint<*, ResponseModel, Resource>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(endPoint) {
        prepareGet(endPoint, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(endPoint)
    }

    protected suspend inline fun <reified ResponseModel, reified Resource : ApiResource> post(
        endPoint: ApiEndPoint<*, ResponseModel, Resource>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(endPoint) {
        preparePost(endPoint, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(endPoint)
    }

    protected suspend inline fun <reified ResponseModel, reified Resource : ApiResource> put(
        endPoint: ApiEndPoint<*, ResponseModel, Resource>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(endPoint) {
        preparePut(endPoint, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(endPoint)
    }

    protected suspend inline fun <reified ResponseModel, reified Resource : ApiResource> submitForm(
        endPoint: ApiEndPoint<*, ResponseModel, Resource>,
        formParameters: Parameters,
        encodeInQuery: Boolean = false,
        method: HttpMethod? = null,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(endPoint) {
        prepareSubmitForm(endPoint, formParameters, encodeInQuery, method, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(endPoint)
    }

    protected suspend inline fun <reified ResponseModel, reified Resource : ApiResource> submitFormWithBinaryData(
        endPoint: ApiEndPoint<*, ResponseModel, Resource>,
        formParameters: List<PartData>,
        encodeInQuery: Boolean = false,
        method: HttpMethod = HttpMethod.Post,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(endPoint) {
        prepareSubmitFormWithBinaryData(endPoint, formParameters, encodeInQuery, method, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(endPoint)
    }


    companion object Helpers {
        protected fun HttpRequestBuilder.addRequestBodyIfExist(apiEndPoint: ApiEndPoint<*, *, *>) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(apiEndPoint.requestBody)
        }

        @OptIn(InternalAPI::class)
        suspend inline fun <reified ResponseBody> HttpResponse.toTypedResponseByResource(resource: ApiEndPoint<*, ResponseBody, *>): HttpTypedResponse<ResponseBody> =
            HttpTypedResponse(
                call = call,
                status = status,
                version = version,
                requestTime = requestTime,
                responseTime = responseTime,
                headers = headers,
                body = body(),
                coroutineContext = coroutineContext,
                rawContent = rawContent
            )
    }
}