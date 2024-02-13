package dev.chara.taskify.convention.plugin

import dev.chara.taskify.convention.Versions

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvmToolchain(Versions.JVM_TOOLCHAIN)
}