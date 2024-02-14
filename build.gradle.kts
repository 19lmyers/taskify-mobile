// Some plugins have to be applied manually...
buildscript {
    repositories {
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
    dependencies {
        classpath(libs.realm.gradle.plugin)
    }
}

// Some plugins expect to be declared exactly once...
plugins {
    alias(libs.plugins.jetbrains.compose) apply false
}