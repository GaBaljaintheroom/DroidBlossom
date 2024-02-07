ALTER TABLE member
    ADD CONSTRAINT UK_PHONE UNIQUE (phone);

ALTER TABLE member
    ADD CONSTRAINT UK_NICKNAME UNIQUE (nickname);

ALTER TABLE member
    ADD CONSTRAINT UK_EMAIL UNIQUE (email);

ALTER TABLE member
    ADD CONSTRAINT UK_AUTH_ID UNIQUE (auth_id);