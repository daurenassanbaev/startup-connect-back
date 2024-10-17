
--liquibase formatted sql
--changeset daurenassanbaev:1
create table if not exists ratings (
                         id SERIAL PRIMARY KEY,
                         user_id VARCHAR(256) NOT NULL,
                         idea_id INT NOT NULL,
                         score INTEGER NOT NULL
);
--changeset daurenassanbaev:2
create table if not exists ideas_files (
                             id SERIAL PRIMARY KEY,
                             user_id VARCHAR(256) NOT NULL,
                             url VARCHAR(256)
);