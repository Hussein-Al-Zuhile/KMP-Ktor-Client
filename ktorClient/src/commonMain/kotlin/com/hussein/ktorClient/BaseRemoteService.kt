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

abstract class BaseRemoteService(protected val client: HttpClient) {

    protected suspend inline fun <reified T : ApiResource<*>> prepareGet(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement =
        client.prepareGet(resource) {
            resource.addRequestBodyIfExist()
            builder()
        }

    protected suspend inline fun <reified T : ApiResource<*>> preparePost(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePost(resource) {
        resource.addRequestBodyIfExist()
        builder()
    }

    protected suspend inline fun <reified T : ApiResource<*>> preparePut(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePut(resource) {
        resource.addRequestBodyIfExist()
        builder()
    }

    protected suspend inline fun <reified T : ApiResource<*>> prepareSubmitForm(
        resource: T,
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

    protected suspend inline fun <reified T : ApiResource<*>> prepareSubmitFormWithBinaryData(
        resource: T,
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

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> get(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareGet(resource, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> post(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePost(resource, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> put(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePut(resource, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> submitForm(
        resource: T,
        formParameters: Parameters,
        encodeInQuery: Boolean = false,
        method: HttpMethod? = null,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareSubmitForm(resource, formParameters, encodeInQuery, method, builder)
            .execute()
            .toTypedResponse<ResponseModel>()
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> submitFormWithBinaryData(
        resource: T,
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
            (this as? ApiResourceWithRequest<*, *>)?.run {
                builder.apply {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
            }
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