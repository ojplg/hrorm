create sequence products_sequence;

create table products_table (
  id integer PRIMARY KEY,
  name text,
  category text,
  price decimal,
  sku integer,
  discontinued boolean,
  first_available timestamp
);