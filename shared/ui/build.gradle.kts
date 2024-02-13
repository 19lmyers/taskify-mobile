plugins {
    id("dev.chara.taskify.convention.plugin.android-library")
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:model"))
            implementation(project(":shared:domain"))
            implementation(project(":shared:component"))

            implementation(libs.atomicfu)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)

            implementation(libs.decompose)
            implementation(libs.decompose.extensions)

            implementation(libs.koin.compose)

            implementation(libs.materialkolor)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.accompanist.permissions)
        }
    }
}

android {
    namespace = "dev.chara.taskify.shared.ui"
}

compose {
    kotlinCompilerPlugin.set(dependencies.compiler.auto)
}