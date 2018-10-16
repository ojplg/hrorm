create sequence immutable_thing_seq;

create table immutable_thing (
    id integer primary key,
    word text,
    amount decimal
);