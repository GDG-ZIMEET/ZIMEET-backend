ALTER TABLE team
    ADD COLUMN verification ENUM('COMPLETE', 'IN_PROGRESS', 'NONE') NOT NULL DEFAULT 'NONE';

ALTER TABLE join_chat
    ADD COLUMN joined_at DATETIME(6) NULL;

ALTER TABLE user_profile
    MODIFY COLUMN emoji VARCHAR(255) NOT NULL;

ALTER TABLE user_profile
    MODIFY COLUMN music ENUM(
    'BALLAD', 'BAND', 'CLASSICAL', 'HIPHOP', 'JAZZ', 'JPOP', 'KPOP', 'POP'
    ) NOT NULL;
