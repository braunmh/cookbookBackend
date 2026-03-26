CREATE TABLE IF NOT EXISTS keyword (
 id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
 parent_id int not null default 0,
 name varchar(255) not null,
 name_upper varchar(255) not null,
 foreign key (parent_id) references keyword(id)
);
CREATE UNIQUE INDEX ix_keyword_name ON keyword(name_upper);

ALTER TABLE keyword
ADD CONSTRAINT ix_keyword_parent
FOREIGN KEY (parent_id)
REFERENCES keyword(`id`);
ON DELETE CASCADE
ON UPDATE CASCADE;

alter table keyword add parent_id int not null default 0
