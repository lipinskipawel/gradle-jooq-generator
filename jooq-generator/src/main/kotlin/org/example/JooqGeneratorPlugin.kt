package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class JooqGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val jooqGeneratorExt = project.extensions.create("jooqGenerator", JooqGeneratorExtension::class.java)
        jooqGeneratorExt.flywayMigrationFiles.setFrom(project.files("src/main/resources/db/migration"))
        jooqGeneratorExt.jooqOutputDirector.convention(project.layout.buildDirectory.dir("generated-sources"))

        project.gradle.sharedServices.registerIfAbsent("postgres", PostgresService::class.java) { spec ->
            spec.parameters.getUser().set("user")
            spec.parameters.getPassword().set("pass")
        }

        project.tasks.register("jooqGenerate", GenerateJooq::class.java) { task ->
            task.inputDirectory.setFrom(jooqGeneratorExt.flywayMigrationFiles)
            task.outputDirectory.convention(jooqGeneratorExt.jooqOutputDirector)

            task.doLast {
                println("Hello from plugin 'org.example.greeting'")
            }
        }
    }
}
