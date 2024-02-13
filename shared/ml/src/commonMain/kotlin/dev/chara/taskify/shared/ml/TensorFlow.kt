package dev.chara.taskify.shared.ml

expect class TFClassifier {
    val classes: List<String>

    fun run(embeddings: FloatArray): FloatArray

    fun close()
}

fun TFClassifier.parse(weights: FloatArray): String? {
    val itemClass = weights.withIndex().maxByOrNull { it.value }?.index ?: -1
    return if (itemClass >= 0 && itemClass < classes.size) {
        classes[itemClass]
    } else {
        null
    }
}