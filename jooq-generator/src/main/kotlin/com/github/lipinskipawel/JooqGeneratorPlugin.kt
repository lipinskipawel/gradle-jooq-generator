package com.github.lipinskipawel

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer

class JooqGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val jooqGeneratorExt = project.extensions.create("jooqGenerator", JooqGeneratorExtension::class.java)
        jooqGeneratorExt.flywayMigrationFiles.setFrom(project.files("src/main/resources/db/migration"))
        jooqGeneratorExt.excludeFlywayTable.convention(true)
        jooqGeneratorExt.initSql.convention("")
        jooqGeneratorExt.jooqOutputDirector.convention(project.layout.buildDirectory.dir("generated-sources"))

        project.gradle.sharedServices.registerIfAbsent("postgres", PostgresService::class.java) { spec ->
            spec.parameters.user.set("user")
            spec.parameters.password.set("pass")
        }

        addJooqClassesToMainSourceSet(project, jooqGeneratorExt)

        project.tasks.register("jooqGenerate", GenerateJooq::class.java) { task ->
            task.inputDirectory.setFrom(jooqGeneratorExt.flywayMigrationFiles)
            task.outputDirectory.convention(jooqGeneratorExt.jooqOutputDirector)

            task.excludeFlywayTable.set(jooqGeneratorExt.excludeFlywayTable)
            task.initSql.set(jooqGeneratorExt.initSql)

            task.syntheticObjectsType.set(jooqGeneratorExt.syntheticObjectsType)

            task.doLast {
                println("Hello from plugin 'jooq-generator'")
            }
        }
    }

    private fun addJooqClassesToMainSourceSet(project: Project, jooqGeneratorExt: JooqGeneratorExtension) {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        main.java.srcDirs(jooqGeneratorExt.jooqOutputDirector)
    }
}
