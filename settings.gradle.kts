pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Taskify"

include(":shared:component")
include(":shared:database")
include(":shared:datastore")
include(":shared:domain")
include(":shared:model")
include(":shared:ml")
include(":shared:network")
include(":shared:ui")

include(":androidApp")
include(":iosFramework")