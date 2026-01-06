package com.hussein.ktorClient

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


interface ApiResource<ResponseBody> {
    val parent: ApiResourceParent?
        get() = null
}

interface ApiResourceWithRequest<RequestBody, ResponseBody> : ApiResource<ResponseBody> {
    @Transient
    val requestBody: RequestBody
}

// Helpers

@Serializable
data object UnitApiResource : ApiResource<Unit>

typealias UnitApiResourceWithRequest<RequestBody> = ApiResourceWithRequest<RequestBody, Unit>
