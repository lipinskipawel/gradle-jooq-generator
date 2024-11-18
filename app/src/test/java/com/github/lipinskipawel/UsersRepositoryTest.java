package com.github.lipinskipawel;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

final class UsersRepositoryTest implements WithAssertions {

    private final UsersRepository repository = new UsersRepository();

    @Test
    void testing_john() {
        final var record = repository.johnUser();

        assertThat(record.getName()).isEqualTo("john");
    }
}
