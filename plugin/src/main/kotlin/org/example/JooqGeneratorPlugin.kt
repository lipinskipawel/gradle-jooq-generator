package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class JooqGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("jooqGenerator") { task ->

            task.doLast {
                println("Hello from plugin 'org.example.greeting'")
            }
        }
    }
}
