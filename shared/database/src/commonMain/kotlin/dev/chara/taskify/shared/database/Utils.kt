package dev.chara.taskify.shared.database

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
    return enumValues<T>().firstOrNull { it.name == name }
}