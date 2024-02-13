plugins {
    id("dev.chara.taskify.convention.plugin.android-library")
    id("org.jetbrains.kotlin.native.cocoapods")
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.firebase.ml.modeldownloader)

            implementation(libs.tensorflow.lite)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    cocoapods {
        name = "TaskifyML"
        version = "1.0.0"
        homepage = "https://taskify.chara.dev"
        summary = "Machine Learning module for Taskify on Apple platforms"
        license = "All Rights Reserved"

        ios.deploymentTarget = "15.2"

        pod(name = "TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")

        framework {
            baseName = "TaskifyML"
            isStatic = true
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
}

android {
    namespace = "dev.chara.taskify.shared.ml"
}