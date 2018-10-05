create sequence columns_seq;

create table columns_table (
  id integer PRIMARY KEY,
  string_column text,
  integer_column integer,
  boolean_column text,
  timestamp_column timestamp,
  color_column text
);