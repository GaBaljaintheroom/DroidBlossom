ALTER TABLE member
    ADD COLUMN password VARCHAR(255);
ALTER TABLE member
    MODIFY email VARCHAR(255) NOT NULL,
    ADD CONSTRAINT unique_email UNIQUE (email);