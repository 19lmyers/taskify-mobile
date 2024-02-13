package dev.chara.taskify.shared.ml

class ClassifierStore(private val loader: ModelLoader) {
    private val classifiers: MutableMap<Model, TFClassifier> = mutableMapOf()

    suspend fun get(model: Model): TFClassifier? = if (classifiers.containsKey(model)) {
        classifiers[model] ?: throw IllegalStateException()
    } else {
        val classifier = loader.loadClassifier(model)
        classifier?.let {
            classifiers[model] = classifier
            classifier
        }
    }

    fun close() {
        for (classifier in classifiers.values) {
            classifier.close()
        }
    }
}