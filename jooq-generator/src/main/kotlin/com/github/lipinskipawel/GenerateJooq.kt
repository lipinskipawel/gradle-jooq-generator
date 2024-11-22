package com.github.lipinskipawel

import org.flywaydb.core.Flyway
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.TaskAction
import org.jooq.codegen.GenerationTool
import org.jooq.codegen.JavaGenerator
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Strategy
import org.jooq.meta.jaxb.SyntheticObjectsType
import org.jooq.meta.jaxb.Target
import java.net.URL
import java.net.URLClassLoader
import javax.sql.DataSource

abstract class GenerateJooq : DefaultTask() {

    @get:ServiceReference("postgres")
    abstract val postgres: Property<PostgresService>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val excludeFlywayTable: Property<Boolean>

    @get:Input
    abstract val initSql: Property<String>

    @get:Input
    abstract val syntheticObjectsType: Property<SyntheticObjectsType>

    @TaskAction
    fun taskAction() {
        val database = postgres.get()
        val dataSource = database.dataSource()

        val classLoader = flywayMigrate(dataSource)
        generateJooq(classLoader, database.jdbc(), database.username(), database.password())
    }

    private fun flywayMigrate(dataSource: DataSource): ClassLoader {
        val extraURLs = HashSet<URL>()
        addClassesAndResourcesDirs(extraURLs)
        extraURLs.forEach { logger.warn(it.toString()) }

        val urlClassLoader = URLClassLoader(
            extraURLs.toTypedArray(),
            project.buildscript.classLoader
        )

        val location = inputDirectory
            .map { "filesystem:" + it.absolutePath }
            .toTypedArray()
        val flyway = Flyway.configure(urlClassLoader)
            .dataSource(dataSource)
            .locations(*location)
            .initSql(initSql.get())
            .load()
        flyway.migrate()
        return urlClassLoader
    }

    private fun addClassesAndResourcesDirs(extraURLs: MutableSet<URL>) {
        val sourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets

        val main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        extraURLs.addAll(main.resources.asFileTree.files
            .stream()
            .map { it.toURI().toURL() }
            .toList()
        )
    }

    private fun generateJooq(classLoader: ClassLoader, jdbc: String, user: String, password: String) {
        project.delete(outputDirectory)
        val canonicalName = JavaGenerator::class.java.canonicalName
        val string = outputDirectory.get().asFile.toString()
        val withJdbc = configureJdbc(jdbc, user, password)
        val withDatabase = configureDatabase()
        val configuration = Configuration()
            .withLogging(Logging.WARN)
            .withJdbc(withJdbc)
            .withGenerator(
                Generator()
                    .withName(canonicalName)
                    .withStrategy(
                        Strategy()
                            .withName("org.jooq.codegen.DefaultGeneratorStrategy")
                    )
                    .withDatabase(withDatabase)
                    .withGenerate(
                        Generate()
                            .withDeprecated(false)
                            .withRecords(true)
                            .withImmutablePojos(true)
                            .withFluentSetters(true)
                    )
                    .withTarget(
                        Target()
                            .withPackageName("org.jooq.codegen")
                            .withDirectory(string)
                            .withClean(true)
                    )
            )

        val generationTool = GenerationTool()
        generationTool.setClassLoader(classLoader)
        generationTool.run(configuration)
    }

    private fun configureJdbc(jdbc: String, user: String, password: String): Jdbc {
        return Jdbc()
            .withDriver("org.postgresql.Driver")
            .withUrl(jdbc)
            .withUser(user)
            .withPassword(password)
    }

    private fun configureDatabase(): Database {
        val database = Database()
            .withName("org.jooq.meta.postgres.PostgresDatabase")
            .withIncludes(".*")
            .withExcludes("")
            .withInputSchema("public")
        if (syntheticObjectsType.isPresent) {
            database.withSyntheticObjects(syntheticObjectsType.get())
        }

        if (excludeFlywayTable.get()) {
            val excludes = database.excludes
            database.excludes = "$excludes|flyway_schema_history"
        }

        return database
    }
}
