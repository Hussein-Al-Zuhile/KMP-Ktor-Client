package com.hussein.ktorClient

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaseRemoteServiceTest {

    @Test
    fun `test post user`() = runBlocking {
        val user = TestUser(1, "Test User")
        val mockEngine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/users/create", request.url.encodedPath)
            respond(
                content = Json.encodeToString(TestUser.serializer(), user),
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(Resources)
            request {
                accept(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json()
            }
        }
        val service = TestService(client)
        val response = service.createUser(user)

        assertTrue(response.status.isSuccess())
        assertEquals(user, response.body)
    }

    @Test
    fun `test put user`() = runBlocking {
        val user = TestUser(1, "Updated User")
        val mockEngine = MockEngine { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("/users/1", request.url.encodedPath)
            respond(
                content = Json.encodeToString(TestUser.serializer(), user),
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(Resources)
            install(ContentNegotiation) {
                json()
            }
        }
        val service = TestService(client)
        val response = service.updateUser(1, user)

        assertTrue(response.status.isSuccess())
        assertEquals(user, response.body)
    }

    @Test
    fun `test submit form`() = runBlocking {
        val mockEngine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/users/form", request.url.encodedPath)
            assertTrue(request.body is FormDataContent)
            respond(
                content = "Form Submitted",
                headers = headersOf("Content-Type", ContentType.Text.Plain.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(Resources)
        }
        val service = TestService(client)
        val params = io.ktor.http.parametersOf("key", "value")
        val response = service.submitUserForm(params)

        assertTrue(response.status.isSuccess())
        assertEquals("Form Submitted", response.body)
    }

    @Test
    fun `test error handling 404`() = runBlocking {
        val user = TestUser(1, "Test User")
        val mockEngine = MockEngine {
            respondError(HttpStatusCode.NotFound)
        }
        val client = HttpClient(mockEngine) {
            expectSuccess = true
            install(Resources)
            install(ContentNegotiation) {
                json()
            }
        }
        val service = TestService(client)
        try {
            service.createUser(user)
            kotlin.test.fail("Should have thrown ClientRequestException")
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            assertEquals(HttpStatusCode.NotFound, e.response.status)
        }
    }
}

@Serializable
data class TestUser(val id: Long, val name: String)

@Serializable
@Resource("/users")
class UserResource : ApiResourceParent {
    @Resource("create")
    data class Create(
        override val parent: UserResource = UserResource(),
        @Transient override val requestBody: TestUser = TestUser(0, "")
    ) :
        ApiResourceWithRequest<TestUser, TestUser>

    @Resource("{id}")
    data class Update(
        override val parent: UserResource = UserResource(),
        val id: Long,
        @Transient override val requestBody: TestUser = TestUser(0, "")
    ) :
        ApiResourceWithRequest<TestUser, TestUser>

    @Resource("form")
    data class Form(override val parent: UserResource = UserResource()) : ApiResource<String>
}

class TestService(client: HttpClient) : BaseRemoteService(client) {
    suspend fun createUser(user: TestUser) = post(UserResource.Create(requestBody = user))
    suspend fun updateUser(id: Long, user: TestUser) = put(UserResource.Update(id = id, requestBody = user))
    suspend fun submitUserForm(params: io.ktor.http.Parameters) = submitForm(UserResource.Form(), params)
}
