package com.github.lipinskipawel

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class JooqGeneratorPluginTest {

    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java-library")
        project.plugins.apply("com.github.lipinskipawel.jooq-generator")

        assertNotNull(project.tasks.findByName("jooqGenerate"))
    }
}
