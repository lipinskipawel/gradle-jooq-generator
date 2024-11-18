package com.github.lipinskipawel

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class JooqGeneratorExtension {

    abstract val flywayMigrationFiles: ConfigurableFileCollection
    abstract val excludeFlywayTable: Property<Boolean>
    abstract val jooqOutputDirector: DirectoryProperty
}
