package com.github.lipinskipawel

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.jooq.meta.jaxb.SyntheticObjectsType

abstract class JooqGeneratorExtension {

    abstract val flywayMigrationFiles: ConfigurableFileCollection
    abstract val excludeFlywayTable: Property<Boolean>
    abstract val initSql: Property<String>

    abstract val jooqOutputDirector: DirectoryProperty
    abstract val syntheticObjectsType: Property<SyntheticObjectsType>
}
