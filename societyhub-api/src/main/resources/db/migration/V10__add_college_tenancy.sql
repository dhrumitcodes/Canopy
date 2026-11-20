CREATE TABLE colleges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email_domain VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_colleges_email_domain UNIQUE (email_domain)
);

-- Preserve existing installations by placing all current records in a default tenant.
INSERT INTO colleges (name, email_domain)
VALUES ('Default College', 'unconfigured.invalid');

ALTER TABLE users ADD COLUMN college_id BIGINT;
ALTER TABLE societies ADD COLUMN college_id BIGINT;

UPDATE users SET college_id = (SELECT id FROM colleges WHERE email_domain = 'unconfigured.invalid');
UPDATE societies SET college_id = (SELECT id FROM colleges WHERE email_domain = 'unconfigured.invalid');

ALTER TABLE users
    ADD CONSTRAINT fk_users_college FOREIGN KEY (college_id) REFERENCES colleges(id);
ALTER TABLE societies
    ALTER COLUMN college_id SET NOT NULL,
    ADD CONSTRAINT fk_societies_college FOREIGN KEY (college_id) REFERENCES colleges(id);

-- Society names need only be unique inside one college, not across the whole platform.
ALTER TABLE societies DROP CONSTRAINT IF EXISTS societies_name_key;
CREATE UNIQUE INDEX uq_societies_college_name
    ON societies (college_id, lower(name));

CREATE INDEX idx_users_college_id ON users(college_id);
CREATE INDEX idx_societies_college_id ON societies(college_id);
