package dev.chara.taskify.shared.ml

import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.tasks.await

class AndroidModelLoader : ModelLoader {
    override suspend fun loadClassifier(model: Model): TFClassifier? {
        val conditions = CustomModelDownloadConditions.Builder().build()

        val result = FirebaseModelDownloader.getInstance()
            .getModel(model.name, DownloadType.LATEST_MODEL, conditions)
            .await()

        val modelFile = result?.file
        return if (modelFile != null) {
            val classifier = TFClassifier(modelFile, model.classes)
            classifier
        } else {
            null
        }
    }
}