package dev.chara.taskify.shared.network.gemini

import dev.chara.taskify.shared.network.gemini.model.ResponsePayload
import dev.chara.taskify.shared.network.gemini.model.createRequestPayload
import dev.chara.taskify.shared.network.gemini.model.parseResponsePayload
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GeminiApiClient(private val apiKey: String) {
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000
        }
    }

    suspend fun generateEmbeddingsFor(text: String): FloatArray? {
        val requestPayload = createRequestPayload(MODEL, text)

        val response = try {
            client.post("https://generativelanguage.googleapis.com/v1beta/models/embedding-001:batchEmbedContents") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }
        } catch (ex: Exception) {
            null
        }

        val responsePayload: ResponsePayload? = response?.body()
        return responsePayload?.let {
            parseResponsePayload(it)
        }
    }

    companion object {
        const val MODEL = "models/embedding-001"
    }
}