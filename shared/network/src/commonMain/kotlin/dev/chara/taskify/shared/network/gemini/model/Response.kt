package dev.chara.taskify.shared.network.gemini.model

import kotlinx.serialization.Serializable

fun parseResponsePayload(payload: ResponsePayload) = payload.embeddings.first().values

@Serializable
data class ResponsePayload(val embeddings: Array<Embeddings>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ResponsePayload

        return embeddings.contentEquals(other.embeddings)
    }

    override fun hashCode(): Int {
        return embeddings.contentHashCode()
    }
}

@Serializable
data class Embeddings(val values: FloatArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Embeddings

        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }
}