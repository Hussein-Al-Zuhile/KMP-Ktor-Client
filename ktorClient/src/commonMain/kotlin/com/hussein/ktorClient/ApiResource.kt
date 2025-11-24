package com.hussein.ktorClient

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable


@Serializable
@Resource("")
sealed interface ApiResource<ResponseBody>

@Serializable
interface ApiResourceWithRequest<RequestBody, ResponseBody> : ApiResource<ResponseBody> {
    val requestBody: RequestBody
}

@Serializable
data object UnitApiResource : ApiResource<Unit>

typealias UnitApiResourceWithRequest<RequestBody> = ApiResourceWithRequest<RequestBody, Unit>
