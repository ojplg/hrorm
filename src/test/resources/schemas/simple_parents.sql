create sequence simple_parent_seq;

create table simple_parent_table (
    id integer PRIMARY KEY,
    name text
);

create sequence simple_child_seq;

create table simple_child_table (
    id integer PRIMARY KEY,
    parent_id integer,
    name text
);
