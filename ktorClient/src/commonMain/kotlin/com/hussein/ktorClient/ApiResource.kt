package com.hussein.ktorClient

import kotlinx.serialization.Serializable


interface ApiResource<ResponseBody> {
    val parent: ApiResourceParent?
        get() = null
}

interface ApiResourceWithRequest<RequestBody, ResponseBody> {
    val parent: ApiResourceParent?
        get() = null
}

// Helpers

@Serializable
data object UnitApiResource : ApiResource<Unit>

typealias UnitApiResourceWithRequest<RequestBody> = ApiResourceWithRequest<RequestBody, Unit>
