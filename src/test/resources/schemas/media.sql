create sequence actors_sequence;
create sequence movies_sequence;
create sequence actor_movie_association_sequence;

create table actors (
    id integer primary key,
    name text
);

create table movies (
    id integer primary key,
    title text
);

create table actor_movie_associations (
    id integer primary key,
    movie_id integer,
    actor_id integer
);