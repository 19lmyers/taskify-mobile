package dev.chara.taskify.shared.ml

interface ModelLoader {
    suspend fun loadClassifier(model: Model): TFClassifier?
}