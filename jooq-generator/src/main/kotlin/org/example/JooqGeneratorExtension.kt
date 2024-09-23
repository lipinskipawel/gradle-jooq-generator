package org.example

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty

interface JooqGeneratorExtension {

    val flywayMigrationFiles: ConfigurableFileCollection
    val jooqOutputDirector: DirectoryProperty
}
