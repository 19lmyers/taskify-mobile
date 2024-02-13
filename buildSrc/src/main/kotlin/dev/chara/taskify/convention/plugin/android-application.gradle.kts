package dev.chara.taskify.convention.plugin

import dev.chara.taskify.convention.Versions

plugins {
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.application")
}

kotlin {
    jvmToolchain(Versions.JVM_TOOLCHAIN)
}

android {
    compileSdk = Versions.COMPILE_SDK
    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
    }
}