package com.hussein.ktorClient

import kotlinx.serialization.Serializable


interface ApiResource<ResponseBody> {
    val parent: ApiResourceParent?
        get() = null
}

interface ApiResourceWithRequest<RequestBody, ResponseBody> : ApiResource<ResponseBody> {

}

// Helpers

@Serializable
data object UnitApiResource : ApiResource<Unit>

typealias UnitApiResourceWithRequest<RequestBody> = ApiResourceWithRequest<RequestBody, Unit>
