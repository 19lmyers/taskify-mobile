plugins {
    id("dev.chara.taskify.convention.plugin.android-library")
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
            implementation(project(":shared:model"))

            implementation(libs.decompose)
            implementation(libs.essenty.coroutines)

            implementation(libs.koin.core)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.result)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "dev.chara.taskify.shared.component"
}