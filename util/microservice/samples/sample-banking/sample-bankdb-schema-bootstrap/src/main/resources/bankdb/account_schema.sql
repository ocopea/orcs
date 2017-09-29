-- Creating the account table
--drop table if exists account;
create table account (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    balance integer NOT NULL
);

-- Creating the customer table
--drop table if exists customer;
create table customer (
  id uuid NOT NULL,
  firstName character varying(255) NOT NULL,
  lastName  character varying(255) NOT NULL
);
