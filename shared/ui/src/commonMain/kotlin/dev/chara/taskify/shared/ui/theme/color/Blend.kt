package dev.chara.taskify.shared.ui.theme.color

import com.materialkolor.hct.Hct
import dev.chara.taskify.shared.ui.theme.color.utils.Cam16
import dev.chara.taskify.shared.ui.theme.color.utils.ColorUtils.lstarFromArgb
import dev.chara.taskify.shared.ui.theme.color.utils.MathUtils
import kotlin.math.min

fun harmonize(designColor: Int, sourceColor: Int): Int {
    val fromHct = Hct.fromInt(designColor)
    val toHct = Hct.fromInt(sourceColor)
    val differenceDegrees = MathUtils.differenceDegrees(fromHct.getHue(), toHct.getHue())
    val rotationDegrees: Double = min(differenceDegrees * 0.5, 15.0)
    val outputHue = MathUtils.sanitizeDegreesDouble(
        fromHct.getHue()
                + rotationDegrees * MathUtils.rotationDirection(fromHct.getHue(), toHct.getHue())
    )
    return Hct.from(outputHue, fromHct.getChroma(), fromHct.getTone()).toInt()
}

fun blend(from: Int, to: Int, amount: Double): Int {
    val ucs = cam16Ucs(from, to, amount)
    val ucsCam = Cam16.fromInt(ucs)
    val fromCam = Cam16.fromInt(from)
    val blended = Hct.from(ucsCam.hue, fromCam.chroma, lstarFromArgb(from))
    return blended.toInt()
}

private fun cam16Ucs(from: Int, to: Int, amount: Double): Int {
    val fromCam = Cam16.fromInt(from)
    val toCam = Cam16.fromInt(to)
    val fromJ = fromCam.jstar
    val fromA = fromCam.astar
    val fromB = fromCam.bstar
    val toJ = toCam.jstar
    val toA = toCam.astar
    val toB = toCam.bstar
    val jstar = fromJ + (toJ - fromJ) * amount
    val astar = fromA + (toA - fromA) * amount
    val bstar = fromB + (toB - fromB) * amount
    return Cam16.fromUcs(jstar, astar, bstar).toInt()
}
