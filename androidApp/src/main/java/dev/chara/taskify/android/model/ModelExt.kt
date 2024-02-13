package dev.chara.taskify.android.model

import androidx.annotation.ColorRes
import dev.chara.taskify.android.R
import dev.chara.taskify.shared.model.SeedColor

val SeedColor.res: Int
    @ColorRes
    get() =
        when (this) {
            SeedColor.Red -> R.color.red
            SeedColor.Orange -> R.color.orange
            SeedColor.Yellow -> R.color.yellow
            SeedColor.Green -> R.color.green
            SeedColor.Blue -> R.color.blue
            SeedColor.Purple -> R.color.purple
            SeedColor.Pink -> R.color.pink
        }