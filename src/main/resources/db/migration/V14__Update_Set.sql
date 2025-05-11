-- 1. [chat_room] UNIQUE 제약 추가, ENUM 값 추가
ALTER TABLE chat_room
    ADD CONSTRAINT uq_random_chat_id UNIQUE (random_chat_id),
    MODIFY chat_type ENUM('RANDOM', 'TEAM', 'USER') NULL;


-- 2. [hi] from_id, to_id 외래키 제약 제거, hi_type 컬럼 추가
ALTER TABLE hi
    DROP FOREIGN KEY FKg8a8236bmo2tpk3fmdang296k,
    DROP FOREIGN KEY FK93t6nv2nrncgegqvf8nof28mh,
    ADD hi_type ENUM('TEAM', 'USER') NULL;


-- 3. [fcm_token] 테이블 생성
create table fcm_token
(
    id           bigint auto_increment
        primary key,
    token        varchar(255) null,
    user_user_id bigint       null,
    created_at   datetime(6)  null,
    updated_at   datetime(6)  null,
    constraint FKe0a0mkcsnkfse7c7pljt8kbn3
        foreign key (user_user_id) references user (user_id)
);

-- 4. [user]  push_agree 컬럼 추가
ALTER TABLE user
    ADD COLUMN push_agree BIT DEFAULT b'0' NOT NULL;


-- 5. [user_profile] hi, is_visible, verification 컬럼 추가
ALTER TABLE user_profile
    ADD COLUMN hi INT NOT NULL DEFAULT 2,
    ADD COLUMN profile_status ENUM('NONE', 'ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'NONE',
    ADD COLUMN verification ENUM('COMPLETE', 'NONE') NOT NULL DEFAULT 'NONE';


-- 6. [User] 생성, 수정 시간 컬럼 추가
ALTER TABLE User
    ADD COLUMN created_at DATETIME(6) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL;
