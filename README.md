
To not run ryuk container
- Set environment variable to disable ryuk container `TESTCONTAINERS_RYUK_DISABLED = true`
- include `org.gradle.daemon=false` inside gradle.properties

Or `./gradlew --stop` to stop the JVM thus stopping the ryuk container.
