package com.hussein.ktorClient

interface ApiEndPoint<RequestBody, ResponseBody, Resource : ApiResource> {
    val resource: Resource
    val requestBody: RequestBody
}

open class UnitRequestApiEndPoint<ResponseBody, Resource : ApiResource>(
    override val resource: Resource,
) : ApiEndPoint<Unit, ResponseBody, Resource> {
    override val requestBody = Unit
}

open class UnitRequestResponseApiEndPoint<Resource : ApiResource>(
    override val resource: Resource,
) : ApiEndPoint<Unit, Unit, Resource> {
    override val requestBody = Unit
}