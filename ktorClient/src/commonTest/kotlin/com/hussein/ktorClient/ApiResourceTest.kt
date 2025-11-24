package com.hussein.ktorClient

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.request
import io.ktor.client.statement.request
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ApiResourceTest {
    @Test
    fun `test get all articles`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = """[{"id":1,"title":"Title 1","body":"Body 1"},{"id":2,"title":"Title 2","body":"Body 2"}]""",
            )
        }
        val client = HttpClient(mockEngine) {
            install(Resources)
        }
        val response = client.request(TestResource.All) {}
        kotlin.test.assertEquals("/all", response.request.url.encodedPath)
    }

    @Test
    fun `test get article by id`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"id":1,"title":"Title 1","body":"Body 1"}""",
            )
        }
        val client = HttpClient(mockEngine) { install(Resources) }
        val response = client.request(TestResource.ById(id = 1)) {}
        kotlin.test.assertEquals("/test/1", response.request.url.encodedPath)
    }
}