ALTER TABLE otp_verifications
    RENAME COLUMN customer_id TO user_id;

ALTER TABLE otp_verifications
DROP COLUMN IF EXISTS account_number;

ALTER TABLE otp_verifications
    ADD COLUMN IF NOT EXISTS email varchar(255);

CREATE INDEX IF NOT EXISTS idx_otp_verifications_user_id
    ON otp_verifications (user_id);