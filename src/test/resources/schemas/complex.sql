
create sequence ANN_SEQUENCE;
create table ANN (
      id integer PRIMARY KEY,
      name text,
      beth_id integer
);

create sequence BETH_SEQUENCE;
create table BETH (
      id integer PRIMARY KEY,
      number integer
);

create sequence CAL_SEQUENCE;
create table CAL (
      id integer PRIMARY KEY,
      amount decimal,
      ann_id integer
);

create sequence DON_SEQUENCE;
create table DON (
      id integer PRIMARY KEY,
      datetime timestamp,
      quantity integer,
      beth_id integer
);

create sequence EDITH_SEQUENCE;
create table EDIT (
      id integer PRIMARY KEY,
      word text,
      length integer,
      beth_id integer,
      fred_id integer,
      gap_id integer
);

create sequence FRED_SEQUENCE;
create table FRED (
      id integer PRIMARY KEY,
      flag text
);

create sequence GAP_SEQUENCE;
create table GAP (
      id integer PRIMARY KEY,
      insignia text
);

create sequence HENRY_SEQUENCE;
create table HENRY (
      id integer PRIMARY KEY,
      fraction decimal,
      amount integer,
      don_id integer,
      ida_id integer
);

create sequence IDA_SEQUENCE;
create table IDA (
      id integer PRIMARY KEY,
      name text,
      jules_id integer
);

create sequence JULES_SEQUENCE;
create table JULES (
      id integer PRIMARY KEY,
      magnitude integer
);
