-- Creating the account table
-- drop table if exists ideas;
create table ideas (
    id varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    docName varchar(255) NOT NULL,
    docKey varchar(255) NOT NULL,
    status varchar(255) NOT NULL,
    votes INTEGER NOT NULL
);