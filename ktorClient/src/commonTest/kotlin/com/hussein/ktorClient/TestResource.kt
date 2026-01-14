package com.hussein.ktorClient

import io.ktor.resources.Resource

@Resource("/test")
internal class TestResource : ApiResourceParent {
    @Resource("all")
    data object All : ApiResource

    @Resource("{id}")
    data class ById(
        override val parent: TestResource = TestResource(),
        val id: Long
    ) : ApiResource

    @Resource("create")
    data class Create(
        override val parent: TestResource = TestResource(),
    ) : ApiResource
}