package com.collektar.features

import com.collektar.dto.*
import com.collektar.features.collection.collectionRoutes
import com.collektar.features.collection.service.CollectionService
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.response.respond
import io.mockk.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class CollectionRoutesTest {

    private val userId = UUID.randomUUID()
    private val service = mockk<CollectionService>(relaxed = true)

    private fun Application.testModule() {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(io.ktor.server.plugins.statuspages.StatusPages) {
            exception<NoSuchElementException> { call, _ ->
                call.respond(HttpStatusCode.NotFound)
            }
            exception<IllegalArgumentException> { call, _ ->
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        routing {
            collectionRoutes(service)
        }
    }


    private fun testClient(block: suspend (io.ktor.client.HttpClient) -> Unit) = testApplication {
        application { testModule() }
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        block(client)
    }

    @Test
    fun getCollections_returnsOK() = testClient { client ->
        val collections = listOf(CollectionInfo("1", "GAMES", false, 123L))
        every { service.listCollections(userId) } returns collections

        val response = client.get("/collections") {
            header("X-User-Id", userId.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        verify { service.ensureDefaults(userId) }
    }

    @Test
    fun postCollections_returnsCreated() = testClient { client ->
        val col = CollectionInfo("1", "MOVIES", false, 0L)
        every { service.createCollection(userId, "MOVIES") } returns col

        val response = client.post("/collections") {
            header("X-User-Id", userId.toString())
            contentType(ContentType.Application.Json)
            setBody(CreateCollectionRequest("MOVIES"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun deleteCollection_returnsOK() = testClient { client ->
        every { service.deleteCollection(userId, "1") } returns true

        val response = client.delete("/collections/1") {
            header("X-User-Id", userId.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun deleteCollection_returnsNotFound() = testClient { client ->
        every { service.deleteCollection(userId, "1") } returns false

        val response = client.delete("/collections/1") {
            header("X-User-Id", userId.toString())
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun patchCollectionVisibility_returnsOK() = testClient { client ->
        every { service.setVisibility(userId, "GAMES", true) } returns Unit

        val response = client.patch("/collections/GAMES/visibility") {
            header("X-User-Id", userId.toString())
            contentType(ContentType.Application.Json)
            setBody(SetCollectionVisibilityRequest(true))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun patchCollectionVisibility_returnsNotFound() = testClient { client ->
        every { service.setVisibility(userId, "GAMES", true) } throws NoSuchElementException()

        val response = client.patch("/collections/GAMES/visibility") {
            header("X-User-Id", userId.toString())
            contentType(ContentType.Application.Json)
            setBody(SetCollectionVisibilityRequest(true))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun postCollectionItem_returnsCreated() = testClient { client ->
        val item = CollectionItemInfo("1", "item1", "Title", null, null, null, 0L)
        every { service.addItem(userId, "GAMES", "item1", "Title", null, null, null) } returns item

        val response = client.post("/collections/GAMES/items") {
            header("X-User-Id", userId.toString())
            contentType(ContentType.Application.Json)
            setBody(AddCollectionItemRequest("item1", "Title"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun postCollectionItem_returnsNotFound() = testClient { client ->
        every { service.addItem(userId, "GAMES", "item1", "Title", null, null, null) } throws NoSuchElementException()

        val response = client.post("/collections/GAMES/items") {
            header("X-User-Id", userId.toString())
            contentType(ContentType.Application.Json)
            setBody(AddCollectionItemRequest("item1", "Title"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getCollectionItems_returnsOK() = testClient { client ->
        every { service.listItems(userId, "GAMES") } returns emptyList()

        val response = client.get("/collections/GAMES/items") {
            header("X-User-Id", userId.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun deleteCollectionItem_returnsOK() = testClient { client ->
        every { service.removeItem(userId, "GAMES", "item1") } just Runs

        val response = client.delete("/collections/GAMES/items/item1") {
            header("X-User-Id", userId.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

}