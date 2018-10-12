
create sequence no_back_sequence;

create table simple_parent (
    id integer primary key,
    name text
);

create table simple_child (
    id integer primary key,
    number integer,
    simple_parent_id integer
);