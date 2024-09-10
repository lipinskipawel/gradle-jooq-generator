package org.example;

import org.flywaydb.core.Flyway;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.jooq.codegen.GenerationTool;
import org.jooq.codegen.JavaGenerator;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Logging;
import org.jooq.meta.jaxb.Strategy;
import org.jooq.meta.jaxb.Target;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

public abstract class GenerateJooq
        extends DefaultTask {

    @ServiceReference("postgres")
    abstract Property<PostgresService> getPostgres();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getInputDirectory();

    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void taskAction() throws Exception {
        final var database = getPostgres().get();
        final var dataSource = database.dataSource();

        final var classLoader = flywayMigrate(dataSource);
        generateJooq(classLoader, database.jdbc(), database.username(), database.password());
    }

    private ClassLoader flywayMigrate(DataSource dataSource) {
        final var extraURLs = new HashSet<URL>();
        addClassesAndResourcesDirs(extraURLs);
        extraURLs.forEach(it -> getLogger().warn(it.toString()));

        final var urlClassLoader = new URLClassLoader(
                extraURLs.toArray(new URL[0]),
                getProject().getBuildscript().getClassLoader()
        );

        final var location = StreamSupport.stream(getInputDirectory().spliterator(), false)
                .map(it -> "filesystem:" + it.getAbsolutePath())
                .toArray(String[]::new);
        final var flyway = Flyway.configure(urlClassLoader)
                .dataSource(dataSource)
                .locations(location)
                .load();
        flyway.migrate();
        return urlClassLoader;
    }

    private void addClassesAndResourcesDirs(Set<URL> extraURLs) {
        final var sourceSets = getProject().getExtensions().getByType(JavaPluginExtension.class).getSourceSets();

        final var main = sourceSets.getByName(MAIN_SOURCE_SET_NAME);
        extraURLs.addAll(main.getResources().getAsFileTree().getFiles().stream().map(it -> {
            try {
                return it.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toList());
    }

    private void generateJooq(ClassLoader classLoader, String jdbc, String user, String password) throws Exception {
        getProject().delete(getOutputDirectory());
        final var canonicalName = JavaGenerator.class.getCanonicalName();
        final var string = getOutputDirectory().get().getAsFile().toString();
        final var configuration = new Configuration()
                .withLogging(Logging.WARN)
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(jdbc)
                        .withUser(user)
                        .withPassword(password))
                .withGenerator(new Generator()
                        .withName(canonicalName)
                        .withStrategy(new Strategy()
                                .withName("org.jooq.codegen.DefaultGeneratorStrategy"))
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("public"))
                        .withGenerate(new Generate()
                                .withDeprecated(false)
                                .withRecords(true)
                                .withImmutablePojos(true)
                                .withFluentSetters(true))
                        .withTarget(new Target()
                                .withPackageName("org.jooq.codegen")
                                .withDirectory(string)
                                .withClean(true)
                        ));

        final var generationTool = new GenerationTool();
        generationTool.setClassLoader(classLoader);
        generationTool.run(configuration);
    }
}
