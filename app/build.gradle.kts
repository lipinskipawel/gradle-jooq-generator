plugins {
    `java-library`
    id("org.example.greeting") version ("0.1.0")
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
