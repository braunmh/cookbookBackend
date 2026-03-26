CREATE TABLE IF NOT EXISTS synonym (
    id INT AUTO_INCREMENT PRIMARY KEY,
    keyword_id int not null,
    name VARCHAR(255) NOT NULL,
    UNIQUE KEY ix_synonym_name (name),
    foreign key (keyword_id) references keyword(id)
);

