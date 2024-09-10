package org.example;

import org.gradle.api.services.BuildService;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

public abstract class PostgresService
        implements BuildService<Params>, AutoCloseable {

    private PostgreSQLContainer<?> postgreSQLContainer;

    public PostgresService() {
        final var user = getParameters().getUser().convention("postgres");
        final var password = getParameters().getPassword().convention("password");

        this.postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.4")
                .withUrlParam("ssl", "false")
                .withUsername(user.get())
                .withPassword(password.get());
        postgreSQLContainer.start();
    }

    public DataSource dataSource() {
        final var dataSource = new PGSimpleDataSource();

        dataSource.setUrl(postgreSQLContainer.getJdbcUrl());
        dataSource.setUser(postgreSQLContainer.getUsername());
        dataSource.setPassword(postgreSQLContainer.getPassword());

        return dataSource;
    }

    public String jdbc() {
        return postgreSQLContainer.getJdbcUrl();
    }

    public String username() {
        return postgreSQLContainer.getUsername();
    }

    public String password() {
        return postgreSQLContainer.getPassword();
    }

    @Override
    public void close() {
        postgreSQLContainer.stop();
    }
}
