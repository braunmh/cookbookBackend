CREATE DATABASE cookbook;
CREATE USER 'cookbook'@'%' IDENTIFIED BY PASSWORD 'your password';
GRANT ALL PRIVILEGES ON cookbook.* TO 'cookbook'@'%' WITH GRANT OPTION;