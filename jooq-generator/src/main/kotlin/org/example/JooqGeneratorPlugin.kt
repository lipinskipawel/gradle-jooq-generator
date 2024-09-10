package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class JooqGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.gradle.sharedServices.registerIfAbsent("postgres", PostgresService::class.java) { spec ->
            spec.parameters.getUser().set("user")
            spec.parameters.getPassword().set("pass")
        }

        project.tasks.register("jooqGenerate", GenerateJooq::class.java) { task ->
            task.logging.captureStandardOutput(LogLevel.DEBUG)
            task.logging.captureStandardError(LogLevel.DEBUG)
            val from = project.objects.fileCollection().from("src/main/resources/db/migration")
            task.inputDirectory.setFrom(from)
            task.outputDirectory.convention(project.layout.buildDirectory.dir("generated-sources"))

            task.doLast {
                println("Hello from plugin 'org.example.greeting'")
            }
        }
    }
}
