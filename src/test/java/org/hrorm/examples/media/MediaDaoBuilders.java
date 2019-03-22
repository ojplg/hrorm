package org.hrorm.examples.media;

import org.hrorm.AssociationDaoBuilder;
import org.hrorm.DaoBuilder;

public class MediaDaoBuilders {

    public static final DaoBuilder<Movie> MOVIE_DAO_BUILDER =
            new DaoBuilder<>("movies", Movie::new)
                    .withPrimaryKey("id", "movies_sequence", Movie::getId, Movie::setId)
                    .withStringColumn("title", Movie::getTitle, Movie::setTitle);


    public static final DaoBuilder<Actor> ACTOR_DAO_BUILDER =
            new DaoBuilder<>("actors", Actor::new)
                    .withPrimaryKey("id", "actors_sequence", Actor::getId, Actor::setId)
                    .withStringColumn("name", Actor::getName, Actor::setName);

    public static final DaoBuilder<ActorMovieAssociation> ACTOR_MOVIE_ASSOCIATION_DAO_BUILDER =
            new DaoBuilder<>("actor_movie_associations", ActorMovieAssociation::new)
                    .withPrimaryKey("id", "actor_movie_association_sequence", ActorMovieAssociation::getId, ActorMovieAssociation::setId)
                    .withJoinColumn("actor_id", ActorMovieAssociation::getActor, ActorMovieAssociation::setActor, ACTOR_DAO_BUILDER)
                    .withJoinColumn("movie_id", ActorMovieAssociation::getMovie, ActorMovieAssociation::setMovie, MOVIE_DAO_BUILDER);


    public static final AssociationDaoBuilder<Actor, Movie> ASSOCIATION_DAO_BUILDER =
            new AssociationDaoBuilder<>(MediaDaoBuilders.ACTOR_DAO_BUILDER, MediaDaoBuilders.MOVIE_DAO_BUILDER)
                    .withTableName("actor_movie_associations")
                    .withSequenceName("actor_movie_association_sequence")
                    .withPrimaryKeyName("id")
                    .withLeftColumnName("actor_id")
                    .withRightColumnName("movie_id");

}
