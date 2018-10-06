create sequence thing_seq;

create table things (
    id integer primary key,
    name text,
    sibling_id integer
);

create sequence sibling_seq;

create table siblings (
    id integer primary key,
    number integer,
    cousin_id integer
);

create sequence cousin_seq;

create table cousins (
    id integer primary key,
    color text
);