create table if not exists job (
    id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type varchar(32) not null,
    message varchar(255) not null,
    information text
);
CREATE UNIQUE INDEX ix_job_type ON job(type);