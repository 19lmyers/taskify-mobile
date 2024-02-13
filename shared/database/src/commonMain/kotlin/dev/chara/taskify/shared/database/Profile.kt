package dev.chara.taskify.shared.database

import dev.chara.taskify.shared.model.Profile
import kotlinx.serialization.Serializable

@Serializable
class CurrentProfile : Profile {
    override val userId: String = ""

    override val email: String = ""
}