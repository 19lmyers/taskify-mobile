package dev.chara.taskify.shared.ui.theme.color.utils

import dev.chara.taskify.shared.ui.theme.color.utils.ColorUtils.argbFromXyz
import dev.chara.taskify.shared.ui.theme.color.utils.ColorUtils.linearized
import dev.chara.taskify.shared.ui.theme.color.utils.MathUtils.signum
import dev.chara.taskify.shared.ui.theme.color.utils.MathUtils.toDegrees
import dev.chara.taskify.shared.ui.theme.color.utils.MathUtils.toRadians
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.expm1
import kotlin.math.hypot
import kotlin.math.ln1p
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal class Cam16 private constructor(
    val hue: Double,
    val chroma: Double,
    val j: Double,
    val q: Double,
    val m: Double,
    val s: Double,
    val jstar: Double,
    val astar: Double,
    val bstar: Double,
) {
    private val tempArray = doubleArrayOf(0.0, 0.0, 0.0)

    fun distance(other: Cam16): Double {
        val dJ = jstar - other.jstar
        val dA = astar - other.astar
        val dB = bstar - other.bstar
        val dEPrime: Double = sqrt(dJ * dJ + dA * dA + dB * dB)
        return 1.41 * dEPrime.pow(0.63)
    }

    fun toInt(): Int {
        return viewed(ViewingConditions.DEFAULT)
    }

    fun viewed(viewingConditions: ViewingConditions): Int {
        val xyz = xyzInViewingConditions(viewingConditions, tempArray)
        return argbFromXyz(xyz[0], xyz[1], xyz[2])
    }

    fun xyzInViewingConditions(viewingConditions: ViewingConditions, returnArray: DoubleArray?): DoubleArray {
        val alpha = if (chroma == 0.0 || j == 0.0) 0.0 else chroma / sqrt(j / 100.0)
        val t: Double = pow(
            alpha / pow(1.64 - pow(0.29, viewingConditions.n), 0.73), 1.0 / 0.9)
        val hRad: Double = toRadians(hue)
        val eHue: Double = 0.25 * (cos(hRad + 2.0) + 3.8)
        val ac: Double = (viewingConditions.aw
                * pow(j / 100.0, 1.0 / viewingConditions.c / viewingConditions.z))
        val p1: Double = eHue * (50000.0 / 13.0) * viewingConditions.nc * viewingConditions.ncb
        val p2: Double = ac / viewingConditions.nbb
        val hSin: Double = sin(hRad)
        val hCos: Double = cos(hRad)
        val gamma = 23.0 * (p2 + 0.305) * t / (23.0 * p1 + 11.0 * t * hCos + 108.0 * t * hSin)
        val a = gamma * hCos
        val b = gamma * hSin
        val rA = (460.0 * p2 + 451.0 * a + 288.0 * b) / 1403.0
        val gA = (460.0 * p2 - 891.0 * a - 261.0 * b) / 1403.0
        val bA = (460.0 * p2 - 220.0 * a - 6300.0 * b) / 1403.0
        val rCBase: Double = max(0.0, 27.13 * abs(rA) / (400.0 - abs(rA)))
        val rC: Double = signum(rA) * (100.0 / viewingConditions.fl) * pow(rCBase, 1.0 / 0.42)
        val gCBase: Double = max(0.0, 27.13 * abs(gA) / (400.0 - abs(gA)))
        val gC: Double = signum(gA) * (100.0 / viewingConditions.fl) * pow(gCBase, 1.0 / 0.42)
        val bCBase: Double = max(0.0, 27.13 * abs(bA) / (400.0 - abs(bA)))
        val bC: Double = signum(bA) * (100.0 / viewingConditions.fl) * pow(bCBase, 1.0 / 0.42)
        val rF: Double = rC / viewingConditions.rgbD.get(0)
        val gF: Double = gC / viewingConditions.rgbD.get(1)
        val bF: Double = bC / viewingConditions.rgbD.get(2)
        val matrix = CAM16RGB_TO_XYZ
        val x = rF * matrix[0][0] + gF * matrix[0][1] + bF * matrix[0][2]
        val y = rF * matrix[1][0] + gF * matrix[1][1] + bF * matrix[1][2]
        val z = rF * matrix[2][0] + gF * matrix[2][1] + bF * matrix[2][2]
        return if (returnArray != null) {
            returnArray[0] = x
            returnArray[1] = y
            returnArray[2] = z
            returnArray
        } else {
            doubleArrayOf(x, y, z)
        }
    }

    companion object {
        val XYZ_TO_CAM16RGB = arrayOf(doubleArrayOf(0.401288, 0.650173, -0.051461), doubleArrayOf(-0.250268, 1.204414, 0.045854), doubleArrayOf(-0.002079, 0.048952, 0.953127))

        val CAM16RGB_TO_XYZ = arrayOf(doubleArrayOf(1.8620678, -1.0112547, 0.14918678), doubleArrayOf(0.38752654, 0.62144744, -0.00897398), doubleArrayOf(-0.01584150, -0.03412294, 1.0499644))

        fun fromInt(argb: Int): Cam16 {
            return fromIntInViewingConditions(argb, ViewingConditions.DEFAULT)
        }

        fun fromIntInViewingConditions(argb: Int, viewingConditions: ViewingConditions): Cam16 {
            // Transform ARGB int to XYZ
            val red = argb and 0x00ff0000 shr 16
            val green = argb and 0x0000ff00 shr 8
            val blue = argb and 0x000000ff
            val redL = linearized(red)
            val greenL = linearized(green)
            val blueL = linearized(blue)
            val x = 0.41233895 * redL + 0.35762064 * greenL + 0.18051042 * blueL
            val y = 0.2126 * redL + 0.7152 * greenL + 0.0722 * blueL
            val z = 0.01932141 * redL + 0.11916382 * greenL + 0.95034478 * blueL
            return fromXyzInViewingConditions(x, y, z, viewingConditions)
        }

        fun fromXyzInViewingConditions(
            x: Double, y: Double, z: Double, viewingConditions: ViewingConditions,
        ): Cam16 {
            val matrix = XYZ_TO_CAM16RGB
            val rT = x * matrix[0][0] + y * matrix[0][1] + z * matrix[0][2]
            val gT = x * matrix[1][0] + y * matrix[1][1] + z * matrix[1][2]
            val bT = x * matrix[2][0] + y * matrix[2][1] + z * matrix[2][2]

            val rD: Double = viewingConditions.rgbD.get(0) * rT
            val gD: Double = viewingConditions.rgbD.get(1) * gT
            val bD: Double = viewingConditions.rgbD.get(2) * bT

            val rAF: Double = pow(viewingConditions.fl * abs(rD) / 100.0, 0.42)
            val gAF: Double = pow(viewingConditions.fl * abs(gD) / 100.0, 0.42)
            val bAF: Double = pow(viewingConditions.fl * abs(bD) / 100.0, 0.42)
            val rA: Double = signum(rD) * 400.0 * rAF / (rAF + 27.13)
            val gA: Double = signum(gD) * 400.0 * gAF / (gAF + 27.13)
            val bA: Double = signum(bD) * 400.0 * bAF / (bAF + 27.13)

            val a = (11.0 * rA + -12.0 * gA + bA) / 11.0
            val b = (rA + gA - 2.0 * bA) / 9.0

            val u = (20.0 * rA + 20.0 * gA + 21.0 * bA) / 20.0
            val p2 = (40.0 * rA + 20.0 * gA + bA) / 20.0

            val atan2: Double = atan2(b, a)
            val atanDegrees: Double = toDegrees(atan2)
            val hue = if (atanDegrees < 0) atanDegrees + 360.0 else if (atanDegrees >= 360) atanDegrees - 360.0 else atanDegrees
            val hueRadians: Double = toRadians(hue)

            val ac: Double = p2 * viewingConditions.nbb

            val j: Double = (100.0
                    * pow(
                ac / viewingConditions.aw,
                viewingConditions.c * viewingConditions.z))
            val q: Double = ((4.0
                    / viewingConditions.c) * sqrt(j / 100.0)
                    * (viewingConditions.aw + 4.0)
                    * viewingConditions.flRoot)

            val huePrime = if (hue < 20.14) hue + 360 else hue
            val eHue: Double = 0.25 * (cos(toRadians(huePrime) + 2.0) + 3.8)
            val p1: Double = 50000.0 / 13.0 * eHue * viewingConditions.nc * viewingConditions.ncb
            val t: Double = p1 * hypot(a, b) / (u + 0.305)
            val alpha: Double = pow(1.64 - pow(0.29, viewingConditions.n), 0.73) * pow(t, 0.9)
            val c: Double = alpha * sqrt(j / 100.0)
            val m: Double = c * viewingConditions.flRoot
            val s: Double = 50.0 * sqrt(alpha * viewingConditions.c / (viewingConditions.aw + 4.0))

            val jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j)
            val mstar: Double = 1.0 / 0.0228 * ln1p(0.0228 * m)
            val astar: Double = mstar * cos(hueRadians)
            val bstar: Double = mstar * sin(hueRadians)
            return Cam16(hue, c, j, q, m, s, jstar, astar, bstar)
        }

        fun fromJch(j: Double, c: Double, h: Double): Cam16 {
            return fromJchInViewingConditions(j, c, h, ViewingConditions.DEFAULT)
        }

        private fun fromJchInViewingConditions(
            j: Double, c: Double, h: Double, viewingConditions: ViewingConditions,
        ): Cam16 {
            val q: Double = ((4.0
                    / viewingConditions.c) * sqrt(j / 100.0)
                    * (viewingConditions.aw + 4.0)
                    * viewingConditions.flRoot)
            val m: Double = c * viewingConditions.flRoot
            val alpha: Double = c / sqrt(j / 100.0)
            val s: Double = 50.0 * sqrt(alpha * viewingConditions.c / (viewingConditions.aw + 4.0))
            val hueRadians: Double = toRadians(h)
            val jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j)
            val mstar: Double = 1.0 / 0.0228 * ln1p(0.0228 * m)
            val astar: Double = mstar * cos(hueRadians)
            val bstar: Double = mstar * sin(hueRadians)
            return Cam16(h, c, j, q, m, s, jstar, astar, bstar)
        }

        fun fromUcs(jstar: Double, astar: Double, bstar: Double): Cam16 {
            return fromUcsInViewingConditions(jstar, astar, bstar, ViewingConditions.DEFAULT)
        }

        fun fromUcsInViewingConditions(
            jstar: Double, astar: Double, bstar: Double, viewingConditions: ViewingConditions,
        ): Cam16 {
            val m: Double = hypot(astar, bstar)
            val m2: Double = expm1(m * 0.0228) / 0.0228
            val c: Double = m2 / viewingConditions.flRoot
            var h: Double = atan2(bstar, astar) * (180.0 / PI)
            if (h < 0.0) {
                h += 360.0
            }
            val j = jstar / (1.0 - (jstar - 100.0) * 0.007)
            return fromJchInViewingConditions(j, c, h, viewingConditions)
        }
    }
}
