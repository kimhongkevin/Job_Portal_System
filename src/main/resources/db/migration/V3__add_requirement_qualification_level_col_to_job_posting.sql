ALTER TABLE job_posting
    ADD deadline date;

ALTER TABLE job_posting
    ADD experience_level VARCHAR(255) CHECK (experience_level IN ('ENTRY','MID','SENIOR','EXECUTIVE'));

ALTER TABLE job_posting
    ADD qualification TEXT;

ALTER TABLE job_posting
    ADD requirement TEXT;
