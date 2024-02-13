package dev.chara.taskify.shared.network.gemini.model

import kotlinx.serialization.Serializable

fun createRequestPayload(model: String, text: String) =
    RequestPayload(arrayOf(Request(model, Content(arrayOf(Part(text))))))

@Serializable
data class RequestPayload(val requests: Array<Request>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RequestPayload

        return requests.contentEquals(other.requests)
    }

    override fun hashCode(): Int {
        return requests.contentHashCode()
    }
}

@Serializable
data class Request(val model: String, val content: Content)

@Serializable
data class Content(val parts: Array<Part>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Content

        return parts.contentEquals(other.parts)
    }

    override fun hashCode(): Int {
        return parts.contentHashCode()
    }
}

@Serializable
data class Part(val text: String)