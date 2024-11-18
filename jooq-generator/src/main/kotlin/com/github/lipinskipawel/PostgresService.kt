package com.github.lipinskipawel

import org.gradle.api.services.BuildService
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

abstract class PostgresService
    : BuildService<Params>, AutoCloseable {

    private var postgreSQLContainer: PostgreSQLContainer<*>

    init {
        val user = parameters.user.convention("postgres")
        val password = parameters.password.convention("postgres")

        postgreSQLContainer = PostgreSQLContainer("postgres:16.4")
            .withUrlParam("ssl", "false")
            .withUsername(user.get())
            .withPassword(password.get())
        postgreSQLContainer.start()
    }

    fun dataSource(): DataSource {
        val dataSource = PGSimpleDataSource()

        dataSource.setURL(postgreSQLContainer.getJdbcUrl())
        dataSource.user = postgreSQLContainer.username
        dataSource.password = postgreSQLContainer.password

        return dataSource
    }

    fun jdbc(): String {
        return postgreSQLContainer.jdbcUrl
    }

    fun username(): String {
        return postgreSQLContainer.username
    }

    fun password(): String {
        return postgreSQLContainer.password
    }

    override fun close() {
        postgreSQLContainer.stop()
    }
}
