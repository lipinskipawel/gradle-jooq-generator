plugins {
    `java-library`
    id("org.example.greeting") version ("0.1.0")
}

repositories {
    mavenCentral()
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
