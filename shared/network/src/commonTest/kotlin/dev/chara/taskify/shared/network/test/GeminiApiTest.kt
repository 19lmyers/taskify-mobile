package dev.chara.taskify.shared.network.test

import dev.chara.taskify.shared.network.gemini.GeminiApiClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GeminiApiTest {
    @Test
    fun generateEmbeddings() = runTest {
        val client = GeminiApiClient()
        val embeddings = client.generateEmbeddingsFor("Hello, world!")
        assertNotNull(embeddings)
        assertEquals(768, embeddings.size)
        //println(embeddings.contentToString())
    }

}