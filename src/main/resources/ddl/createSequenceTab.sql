CREATE TABLE IF NOT EXISTS sequencetab (
    name VARCHAR(255) NOT NULL PRIMARY KEY,
    lastSeq int not null,
    allocatation int NOT NULL default 10
);
insert into sequencetab (name, lastSeq, allocatation) values ('Recipe', 0, 10);

select * from keyword where name = 'Root'
