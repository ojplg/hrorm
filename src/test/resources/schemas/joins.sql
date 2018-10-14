create sequence thing_seq;

create table things (
    thing_id integer primary key,
    name text,
    sibling_id integer
);

create sequence sibling_seq;

create table siblings (
    sibling_id integer primary key,
    number integer,
    cousin_id integer
);

create sequence cousin_seq;

create table cousins (
    cousin_id integer primary key,
    color text,
    second_cousin_id integer
);

create sequence second_cousin_seq;

create table second_cousins (
    second_cousin_id integer primary key,
    datetime timestamp
);