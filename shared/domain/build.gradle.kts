import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

val networkProperties = Properties()
val propertiesFile: File = project.rootProject.file("network.properties")
if (propertiesFile.exists()) {
    networkProperties.load(propertiesFile.inputStream())
}

plugins {
    id("dev.chara.taskify.convention.plugin.android-library")
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:database"))
            implementation(project(":shared:datastore"))
            implementation(project(":shared:ml"))
            implementation(project(":shared:model"))
            implementation(project(":shared:network"))

            implementation(libs.koin.core)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.okio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

buildkonfig {
    packageName = "dev.chara.taskify.shared.domain"

    defaultConfigs {
        buildConfigField(STRING, "atlasAppId", networkProperties.getProperty("atlas.app_id"))
        buildConfigField(STRING, "geminiApiKey", networkProperties.getProperty("gemini.api_key"))
    }
}

android {
    namespace = "dev.chara.taskify.shared.domain"
}