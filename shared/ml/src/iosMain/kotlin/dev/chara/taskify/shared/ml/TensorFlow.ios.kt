package dev.chara.taskify.shared.ml

import cocoapods.TFLTensorFlowLite.TFLInterpreter
import cocoapods.TFLTensorFlowLite.TFLInterpreterOptions
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.Foundation.NSMutableData
import platform.Foundation.dataWithBytes
import platform.Foundation.getBytes

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class TFClassifier(modelPath: String, actual val classes: List<String>) {

    private val interpreter = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val options = TFLInterpreterOptions()

        val interpreter = TFLInterpreter(modelPath, options, error.ptr)
        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        interpreter
    }

    actual fun run(embeddings: FloatArray): FloatArray = memScoped {
        // Allocate error pointer
        val error = alloc<ObjCObjectVar<NSError?>>()

        // Allocate tensors
        interpreter.allocateTensorsWithError(error.ptr)

        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        // Get input tensor
        val inputTensor = interpreter.inputTensorAtIndex(0u, error.ptr)
            ?: throw NullPointerException("Input tensor is null")

        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        // Create native array ptr to hold data
        val inputPtr = allocArrayOf(*embeddings)

        // Copy native array ptr contents to tensor
        val input = NSMutableData.dataWithBytes(inputPtr, 3072u)
        inputTensor.copyData(input, error.ptr)

        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        // Invoke classifier
        interpreter.invokeWithError(error.ptr)

        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        // Get output tensor
        val outputTensor = interpreter.outputTensorAtIndex(0u, error.ptr)
            ?: throw NullPointerException("Output tensor is null")

        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        // Get data from output tensor
        val output = outputTensor.dataWithError(error.ptr)
            ?: throw NullPointerException("Output NSData is null")

        if (error.value != null) {
            throw IllegalStateException(error.value?.debugDescription)
        }

        // Create native array ptr to hold output
        val outputPtr = allocArray<FloatVar>(classes.size)

        // Copy NSData to native memory
        output.getBytes(outputPtr)

        // Define Kotlin array for final weights
        val weights = FloatArray(classes.size)

        // Read weights from native array
        for (i in 0..<classes.size) {
            weights[i] = outputPtr[i]
        }

        return weights
    }

    actual fun close() {
        interpreter.finalize()
    }
}