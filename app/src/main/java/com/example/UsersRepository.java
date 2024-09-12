package com.example;

import org.jooq.TableField;
import org.jooq.codegen.tables.records.UsersRecord;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.jooq.codegen.tables.Users.USERS;

public final class UsersRepository {

    UsersRecord johnUser() {
        UsersRecord john = new UsersRecord(randomUUID(), "john");
        return john;
    }

    TableField<UsersRecord, UUID> id() {
        return USERS.ID;
    }
}
