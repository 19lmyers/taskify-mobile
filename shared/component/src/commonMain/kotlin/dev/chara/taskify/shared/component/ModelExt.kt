package dev.chara.taskify.shared.component

import dev.chara.taskify.shared.model.Id
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class IdSerializer : KSerializer<Id> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Id", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Id {
        val hexString = decoder.decodeString()
        return SerializedId(hexString)
    }

    override fun serialize(encoder: Encoder, value: Id) {
        encoder.encodeString(value.hexString)
    }

}

internal class SerializedId(override val hexString: String) : Id()