plugins {
    `java-library`
    id("com.github.lipinskipawel.jooq-generator") version ("0.1.0")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:3.19.11")
}

tasks.named("compileJava") {
    dependsOn("jooqGenerate")
}

jooqGenerator {
    excludeFlywayTable = true
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.testing.assertj)
            }
        }
    }
}
