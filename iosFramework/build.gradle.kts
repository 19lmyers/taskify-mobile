plugins {
    id("dev.chara.taskify.convention.plugin.native-library")
    id("org.jetbrains.kotlin.native.cocoapods")
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:component"))
            api(project(":shared:domain"))
            api(project(":shared:ui"))

            api(libs.decompose)
            api(libs.essenty)

            implementation(libs.koin.core)
        }
    }

    cocoapods {
        name = "Taskify"
        version = "1.0.0"
        homepage = "https://taskify.chara.dev"
        summary = "Framework for Taskify on Apple platforms"
        license = "All Rights Reserved"

        ios.deploymentTarget = "15.2"

        // Redeclare transitive pod dependencies for linker
        //pod(name = "TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite", linkOnly = true)

        framework {
            baseName = "TaskifyShared"
            isStatic = true

            export(project(":shared:component"))
            export(project(":shared:domain"))
            export(project(":shared:ui"))

            export(libs.decompose)
            export(libs.essenty)
        }
    }
}