create table if not exists "UserTable" (
    "uuid" uuid NOT NULL PRIMARY KEY,
    "name" VARCHAR(255),
    "age" INT
)