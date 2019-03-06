create sequence products_sequence;

create table products (
  id integer PRIMARY KEY,
  name text,
  category text,
  price decimal,
  sku integer,
  discontinued boolean,
  first_available timestamp
);