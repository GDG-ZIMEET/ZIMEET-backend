
ALTER TABLE fcm_token
    DROP FOREIGN KEY FKe0a0mkcsnkfse7c7pljt8kbn3,
    ADD CONSTRAINT UC_user_user_id UNIQUE (user_user_id),
    MODIFY token VARCHAR(255) NOT NULL;