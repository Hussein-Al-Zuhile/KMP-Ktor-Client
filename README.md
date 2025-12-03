# KMP Ktor Client Library

This library provides a simplified and structured way to interact with REST APIs in your Kotlin Multiplatform projects,
built on top of Ktor. It offers a type-safe approach to defining API endpoints and streamlines making HTTP requests.

## Features

- **Type-Safe API Endpoints**: Define your API endpoints as typed resources.
- **Simplified HTTP Requests**: Abstract away the boilerplate of making `get`, `post`, `put` and form requests.
- **Pre-configured Ktor Client**: Includes a factory function to create a sensible default `HttpClient`.
- **Typed Responses**: Get back a `HttpTypedResponse` that includes the deserialized body.

## Getting Started

Add the dependency to your module's `build.gradle.kts` file:

```groovy
dependencies {
    implementation("io.github.hussein-al-zuhile:kmp-ktor-client:1.0.0")
}
```

### 2. Define API Resources

Create a class for each resource root. Inside, define your specific endpoints as `data class` or `object` properties
that implement `ApiResource<ResponseBody>`.

For requests with a body, use the `ApiResourceWithRequest<RequestBody, ResponseBody>` interface.

```kotlin
import com.hussein.ktorClient.ApiResource
import com.hussein.ktorClient.ApiResourceParent
import com.hussein.ktorClient.ApiResourceWithRequest
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class UserResource : ApiResourceParent {
    @Resource("all")
    data class All(val active: Boolean) : ApiResource<List<User>>

    @Resource("{id}")
    data class ById(val parent: UserResource = UserResource(), val id: Long) : ApiResource<User>

    @Resource("create")
    data class Create(val parent: UserResource = UserResource(), override val requestBody: User) :
        ApiResourceWithRequest<User, Unit>
}

@Serializable
data class User(val id: Long, val name: String)
```

### 3. Create an Http Service

Create a class that inherits from `BaseHttpService` and inject an `HttpClient`. This class will contain the functions
that make the actual API calls.

```kotlin
import com.hussein.ktorClient.BaseHttpService
import io.ktor.client.HttpClient

class UserHttpService(client: HttpClient) : BaseHttpService(client) {

    suspend fun getUsers(active: Boolean) = get(
        resource = UserResource.All(active = active)
    )

    suspend fun getUserById(id: Long) = get(
        resource = UserResource.ById(id = id)
    )

    suspend fun createUser(user: User) = post(
        resource = UserResource.Create(requestBody = user)
    )
}
```

### 4. Create the HttpClient

Use the `defaultJsonHttpClient` function to create a pre-configured `HttpClient`. You can customize it further if
needed.

```kotlin
import com.hussein.ktorClient.defaultJsonHttpClient

val myClient = defaultJsonHttpClient(
    baseUrl = "https://api.example.com",
    isLoggingEnabled = true,
    accessToken = "your-bearer-token"
) {
    // Extra Ktor configuration can be added here
}
```

### 5. Make API Calls

Now, you can instantiate your service and make API calls.

```kotlin
val userService = UserHttpService(myClient)

// Make a GET request
val usersResponse = userService.getUsers(active = true)
if (usersResponse.status.isSuccess()) {
    val users: List<User> = usersResponse.body
    println("Got users: $users")
}

// Make a POST request
val newUser = User(id = 0, name = "John Doe")
val createResponse = userService.createUser(newUser)
if (createResponse.status.isSuccess()) {
    println("User created successfully!")
}
```
