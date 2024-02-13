import java.util.Properties

val localProperties = Properties()
val propertiesFile: File = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    localProperties.load(propertiesFile.inputStream())
}

plugins {
    id("dev.chara.taskify.convention.plugin.android-application")

    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "dev.chara.taskify.android"
    defaultConfig {
        applicationId = "dev.chara.taskify.android"
        versionCode = 1
        versionName = "0.5"
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":shared:component"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:model"))
    implementation(project(":shared:ui"))

    implementation(libs.androidx.activity)

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.lifecycle.service)

    implementation(libs.androidx.work)

    implementation(libs.decompose)

    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.kotlinx.coroutines.play.services)
}

compose {
    kotlinCompilerPlugin.set(dependencies.compiler.auto)
}