create sequence columns_seq;

create table columns_table (
  id integer PRIMARY KEY,
  string_column text,
  integer_column integer,
  decimal_column decimal,
  boolean_column boolean,
  timestamp_column timestamp,
  color_column text
);