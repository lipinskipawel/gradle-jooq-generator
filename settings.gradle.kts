pluginManagement {
    includeBuild("plugin")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "jooq-generator"

include("app")
