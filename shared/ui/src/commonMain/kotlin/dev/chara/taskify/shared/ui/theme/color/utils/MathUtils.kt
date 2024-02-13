package dev.chara.taskify.shared.ui.theme.color.utils

import kotlin.math.abs

internal object MathUtils {

    fun signum(num: Double): Int {
        return if (num < 0) {
            -1
        } else if (num == 0.0) {
            0
        } else {
            1
        }
    }

    fun lerp(start: Double, stop: Double, amount: Double): Double {
        return (1.0 - amount) * start + amount * stop
    }

    fun clampInt(min: Int, max: Int, input: Int): Int {
        if (input < min) {
            return min
        } else if (input > max) {
            return max
        }
        return input
    }

    fun clampDouble(min: Double, max: Double, input: Double): Double {
        if (input < min) {
            return min
        } else if (input > max) {
            return max
        }
        return input
    }

    fun sanitizeDegreesInt(degrees: Int): Int {
        var sanitized = degrees % 360
        if (sanitized < 0) {
            sanitized = sanitized + 360
        }
        return sanitized
    }

    fun sanitizeDegreesDouble(degrees: Double): Double {
        var sanitized = degrees % 360.0
        if (sanitized < 0) {
            sanitized = sanitized + 360.0
        }
        return sanitized
    }

    fun rotationDirection(from: Double, to: Double): Double {
        val increasingDifference = sanitizeDegreesDouble(to - from)
        return if (increasingDifference <= 180.0) 1.0 else -1.0
    }


    fun differenceDegrees(a: Double, b: Double): Double {
        return 180.0 - abs(abs(a - b) - 180.0)
    }

    fun matrixMultiply(row: DoubleArray, matrix: Array<DoubleArray>): DoubleArray {
        val a = row[0] * matrix[0][0] + row[1] * matrix[0][1] + row[2] * matrix[0][2]
        val b = row[0] * matrix[1][0] + row[1] * matrix[1][1] + row[2] * matrix[1][2]
        val c = row[0] * matrix[2][0] + row[1] * matrix[2][1] + row[2] * matrix[2][2]
        return doubleArrayOf(a, b, c)
    }

    fun toDegrees(angrad: Double): Double {
        return angrad * 57.29577951308232
    }

    fun toRadians(angdeg: Double): Double {
        return angdeg * 0.017453292519943295
    }
}