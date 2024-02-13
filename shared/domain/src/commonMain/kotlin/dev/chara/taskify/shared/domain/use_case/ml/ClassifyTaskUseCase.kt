package dev.chara.taskify.shared.domain.use_case.ml

import dev.chara.taskify.shared.ml.ClassifierStore
import dev.chara.taskify.shared.ml.Model
import dev.chara.taskify.shared.ml.parse
import dev.chara.taskify.shared.model.Category
import dev.chara.taskify.shared.network.gemini.GeminiApiClient

class ClassifyTaskUseCase(
    private val apiClient: GeminiApiClient,
    private val classifierStore: ClassifierStore
) {
    suspend operator fun invoke(name: String, type: Category.Classifier?): String? {
        val model = when (type) {
            Category.Classifier.Grocery -> Model.ClassifierGrocery
            else -> null
        }

        return model?.let {
            val embeddings = apiClient.generateEmbeddingsFor(name) ?: return null

            val classifier = classifierStore.get(model)

            classifier?.let {
                val weights = it.run(embeddings)
                it.parse(weights)
            }
        }
    }
}