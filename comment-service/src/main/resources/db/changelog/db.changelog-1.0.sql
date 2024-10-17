
--liquibase formatted sql
--changeset daurenassanbaev:1
create table if not exists comments (
                          id SERIAL PRIMARY KEY,
                          user_id VARCHAR(256) NOT NULL,
                          idea_id INTEGER NOT NULL,
                          content TEXT NOT NULL
);