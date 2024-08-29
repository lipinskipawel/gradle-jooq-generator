pluginManagement {
    includeBuild("jooq-generator")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "gradle-jooq-generator"

include("app")
