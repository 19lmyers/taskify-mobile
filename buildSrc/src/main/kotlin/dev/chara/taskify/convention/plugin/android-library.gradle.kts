package dev.chara.taskify.convention.plugin

import dev.chara.taskify.convention.Versions

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.library")
}

kotlin {
    jvmToolchain(Versions.JVM_TOOLCHAIN)
}

android {
    compileSdk = Versions.COMPILE_SDK
    defaultConfig {
        minSdk = Versions.MIN_SDK
    }
}