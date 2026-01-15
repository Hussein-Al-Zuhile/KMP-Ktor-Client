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

abstract class BaseHttpService(protected val client: HttpClient) {

    protected suspend inline fun <reified T : ApiResource<*>> prepareGet(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement =
        client.prepareGet(resource) {
            builder()
        }

    protected suspend inline fun <reified T : ApiResourceWithRequest<RequestBody, *>, reified RequestBody> prepareGet(
        resource: T,
        requestBody: RequestBody,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement =
        client.prepareGet(resource) {
            setBody(requestBody)
            builder()
        }

    protected suspend inline fun <reified T : ApiResource<*>> preparePost(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePost(resource) {
        builder()
    }

    protected suspend inline fun <reified T : ApiResourceWithRequest<RequestBody, *>, reified RequestBody> preparePost(
        resource: T,
        requestBody: RequestBody,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePost(resource) {
        setBody(requestBody)
        builder()
    }

    protected suspend inline fun <reified T : ApiResource<*>> preparePut(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePut(resource) {
        builder()
    }

    protected suspend inline fun <reified T : ApiResourceWithRequest<RequestBody, *>, reified RequestBody> preparePut(
        resource: T,
        requestBody: RequestBody,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpStatement = client.preparePut(resource) {
        setBody(requestBody)
        builder()
    }

    protected suspend inline fun <reified T : ApiResource<*>> prepareSubmitForm(
        resource: T,
        formParameters: Parameters,
        encodeInQuery: Boolean = false,
        method: HttpMethod? = null,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = client.prepareRequest(resource) {
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
        method: HttpMethod = HttpMethod.Post,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = client.prepareRequest(resource) {
        this.method = method
        setBody(MultiPartFormDataContent(formParameters))
        builder()
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> get(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareGet(resource, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
    }

    protected suspend inline fun <reified T : ApiResourceWithRequest<RequestBody, ResponseModel>, reified RequestBody, reified ResponseModel> get(
        resource: T,
        requestBody: RequestBody,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareGet(resource, requestBody, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> post(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePost(resource, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
    }

    protected suspend inline fun <reified T : ApiResourceWithRequest<RequestBody, ResponseModel>, reified RequestBody, reified ResponseModel> post(
        resource: T,
        requestBody: RequestBody,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePost(resource, requestBody, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> put(
        resource: T,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePut(resource, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
    }

    protected suspend inline fun <reified T : ApiResourceWithRequest<RequestBody, ResponseModel>, reified RequestBody, reified ResponseModel> put(
        resource: T,
        requestBody: RequestBody,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        preparePut(resource, requestBody, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
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
            .toTypedResponseByResource<ResponseModel>(resource)
    }

    protected suspend inline fun <reified T : ApiResource<ResponseModel>, reified ResponseModel> submitFormWithBinaryData(
        resource: T,
        formParameters: List<PartData>,
        method: HttpMethod = HttpMethod.Post,
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = with(resource) {
        prepareSubmitFormWithBinaryData(resource, formParameters, method, builder)
            .execute()
            .toTypedResponseByResource<ResponseModel>(resource)
    }


    companion object Helpers {

        @OptIn(InternalAPI::class)
        suspend inline fun <reified ResponseBody> HttpResponse.toTypedResponseByResource(resource: ApiResource<ResponseBody>): HttpTypedResponse<ResponseBody> =
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