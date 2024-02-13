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
            implementation(project(":shared:model"))

            implementation(libs.androidx.datastore)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)

            implementation(libs.okio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "dev.chara.taskify.shared.datastore"
}