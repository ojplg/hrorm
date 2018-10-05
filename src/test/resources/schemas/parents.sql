create sequence parent_seq;

create table parent_table (
    id integer PRIMARY KEY,
    name text
);

create sequence child_seq;

create table child_table (
    id integer PRIMARY KEY,
    parent_table_id integer,
    number integer
);

create sequence grandchild_seq;

create table grandchild_table (
    id integer PRIMARY KEY,
    child_table_id integer,
    color text
);
