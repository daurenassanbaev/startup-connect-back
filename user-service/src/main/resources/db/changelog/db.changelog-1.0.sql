
--liquibase formatted sql
--changeset daurenassanbaev:1
CREATE TABLE IF NOT EXISTS photos (
                        id SERIAL PRIMARY KEY,
                        user_id VARCHAR(256) NOT NULL,
                        url VARCHAR(256)
);