package dev.chara.taskify.shared.ui.ext

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import dev.chara.taskify.shared.model.Id

val idStateSaver
    get() = Saver<MutableState<Id?>, String>(
        save = { it.value?.hexString },
        restore = { mutableStateOf(SavedId(it)) }
    )


internal class SavedId(override val hexString: String) : Id()