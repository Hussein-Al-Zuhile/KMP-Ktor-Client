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
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.utils.io.InternalAPI

abstract class BaseRemoteService(protected val client: HttpClient) {

    protected suspend inline fun prepareGet(
        resource: ApiResource<*>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement =
        client.prepareGet(resource) {
            resource.addRequestBodyIfExist()
            builder()
        }

    protected suspend inline fun preparePost(
        resource: ApiResource<*>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePost(resource) {
        resource.addRequestBodyIfExist()
        builder()
    }

    protected suspend inline fun preparePut(
        resource: ApiResource<*>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePut(resource) {
        resource.addRequestBodyIfExist()
        builder()
    }

    protected suspend inline fun prepareSubmitForm(
        resource: ApiResource<*>,
        formParameters: Parameters,
        encodeInQuery: Boolean = false,
        method: HttpMethod? = null,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = client.prepareRequest(resource) {
        resource.addRequestBodyIfExist()
        if (encodeInQuery) {
            this.method = method ?: HttpMethod.Get
            url.parameters.appendAll(formParameters)
        } else {
            this.method = method ?: HttpMethod.Post
            setBody(FormDataContent(formParameters))
        }
        builder()
    }

    protected suspend inline fun prepareSubmitFormWithBinaryData(
        resource: ApiResource<*>,
        formParameters: List<PartData>,
        encodeInQuery: Boolean = false,
        method: HttpMethod = HttpMethod.Post,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = client.prepareRequest(resource) {
        this.method = method
        resource.addRequestBodyIfExist()
        setBody(MultiPartFormDataContent(formParameters))
        builder()
    }

    protected suspend inline fun <reified ResponseModel> get(
        resource: ApiResource<ResponseModel>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareGet(resource, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified ResponseModel> post(
        resource: ApiResource<ResponseModel>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePost(resource, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified ResponseModel> put(
        resource: ApiResource<ResponseModel>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePut(resource, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified ResponseModel> submitForm(
        resource: ApiResource<ResponseModel>,
        formParameters: Parameters,
        encodeInQuery: Boolean = false,
        method: HttpMethod? = null,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareSubmitForm(resource, formParameters, encodeInQuery, method, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified ResponseModel> submitFormWithBinaryData(
        resource: ApiResource<ResponseModel>,
        formParameters: List<PartData>,
        encodeInQuery: Boolean = false,
        method: HttpMethod = HttpMethod.Post,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareSubmitFormWithBinaryData(resource, formParameters, encodeInQuery, method, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }


    companion object Helpers {
        context(builder: HttpRequestBuilder)
        protected fun ApiResource<*>.addRequestBodyIfExist() {
            (this as? ApiResourceWithRequest<*, *>)?.run { builder.setBody(requestBody) }
        }

        @OptIn(InternalAPI::class)
        context(_: ApiResource<ResponseBody>)
        suspend inline fun <reified ResponseBody> HttpResponse.toTypedResponse(): HttpTypedResponse<ResponseBody> =
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