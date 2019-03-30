create sequence booleans_sequence;

create table booleans_table (
    id integer primary key,
    string_column text,
    integer_column integer,
    boolean_column boolean,
    integer_object_column integer,
    string_object_column text,
    object_column boolean
);