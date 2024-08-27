package org.example

import org.flywaydb.core.Flyway
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
import org.testcontainers.containers.PostgreSQLContainer

class JooqGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("jooqGenerator") { task ->
            val user = "postgres"
            val password = "postgres"
            val port = 6543

            val postgresSQLContainer = PostgreSQLContainer("postgres:16.4")
                .withUsername(user)
                .withPassword(password)
            postgresSQLContainer.addExposedPort(port)
            postgresSQLContainer.start()

            Flyway.configure()
                .dataSource(postgresSQLContainer.jdbcUrl, user, password)
                .locations("src/main/resources/db/migration")
                .load()
                .migrate()

            val configuration = Configuration()
                .withJdbc(
                    Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(postgresSQLContainer.jdbcUrl)
                        .withUser(user)
                        .withPassword(password)
                )
                .withGenerator(
                    Generator()
                        .withDatabase(
                            Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("public")
                        )
                        .withTarget(
                            Target()
                                .withPackageName("org.jooq.codegen")
                                .withDirectory("build/generated-sources/jooq")
                        )
                )

            GenerationTool.generate(configuration)

            task.doLast {
                println("Hello from plugin 'org.example.greeting'")
            }
        }
    }
}
