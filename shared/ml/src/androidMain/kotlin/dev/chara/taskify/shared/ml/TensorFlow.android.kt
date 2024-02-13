package dev.chara.taskify.shared.ml

import org.tensorflow.lite.Interpreter
import java.io.File

actual class TFClassifier(model: File, actual val classes: List<String>) {
    private val interpreter = Interpreter(model)

    actual fun run(embeddings: FloatArray): FloatArray {
        val input = arrayOf(embeddings)
        val output = mutableMapOf(0 to arrayOf(FloatArray(classes.size)))

        interpreter.runForMultipleInputsOutputs(input, output as Map<Int, Any>)
        return output[0]!![0]
    }

    actual fun close() {
        interpreter.close()
    }
}