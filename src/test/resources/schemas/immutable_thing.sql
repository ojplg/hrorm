create sequence immutable_thing_seq;

create table immutable_thing (
    id integer primary key,
    word text,
    amount decimal
);

create sequence immutable_child_seq;

create table immutable_child (
    id integer primary key,
    birthday timestamp,
    flag integer,
    sibling_id integer,
    thing_id integer
);

create sequence immutable_sibling_seq;

create table immutable_sibling (
    id integer primary key,
    data text
);